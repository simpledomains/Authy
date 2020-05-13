<!--suppress JSUnresolvedVariable -->
<template>
    <v-auth-app location="Profile - Security">
        <v-content>
            <v-container>
                <v-row>
                    <v-col>
                        <v-card>
                            <v-card-title>My Profile - Security</v-card-title>
                            <v-card-subtitle>
                                Security related options like 2FA and mTLS is configured here.
                            </v-card-subtitle>
                            <v-divider/>

                            <!-- SECTION: PASSWORD -->
                            <v-card-subtitle>
                                Password Settings
                            </v-card-subtitle>
                            <v-card-text>
                                <v-btn small outlined color="orange" @click="changePassword">
                                    <v-icon left>mdi-key-change</v-icon>
                                    Change my Password
                                </v-btn>
                            </v-card-text>
                            <v-divider/>
                            <!-- END SECTION: PASSWORD -->

                            <!-- SECTION: 2FA -->
                            <v-card-subtitle>2FA Settings</v-card-subtitle>
                            <v-card-text>
                                <v-btn v-if="otpEnabled" color="error" dark small @click="disableTwoFactor">
                                    <v-icon left>mdi-delete-empty-outline</v-icon>
                                    Disable 2FA
                                </v-btn>
                                <v-btn v-else color="secondary" dark small @click="enableTwoFactor">
                                    <v-icon left>mdi-sticker-check-outline</v-icon>
                                    Enable 2FA
                                </v-btn>
                            </v-card-text>
                            <v-divider/>
                            <!-- END SECTION: 2FA -->

                            <!-- SECTION: mTLS -->
                            <v-card-subtitle v-if="isTLSEnabled">mTLS Settings</v-card-subtitle>
                            <v-data-table v-if="isTLSEnabled"
                                          :loading="certificatesFetching"
                                          :items="certificates"
                                          :headers="certificateTableHeaders">
                                <template v-slot:item.name="{ item }">
                                    <i>{{ item.name }}</i>
                                </template>
                                <template v-slot:item.actions="{ item }">
                                    <v-btn icon color="error" v-if="item.revokedAt == null"
                                           @click="revokeCertificate(item.serial)">
                                        <v-icon>mdi-delete</v-icon>
                                    </v-btn>
                                </template>
                                <template v-slot:item.revokedAt="{ item }">
                                    <v-btn x-small color="success" v-if="item.revokedAt == null" dark>NOT REVOKED YET
                                    </v-btn>
                                    <v-btn x-small color="error" v-else dark :title="timeSince(item.revokedAt)">{{
                                        formatDate(item.revokedAt) }}
                                    </v-btn>
                                </template>
                                <template v-slot:item.issuedAt="{ item }">
                                    <v-btn x-small color="primary" dark :title="timeSince(item.issuedAt)">{{
                                        formatDate(item.issuedAt) }}
                                    </v-btn>
                                </template>
                                <template v-slot:item.lastAccess="{ item }">
                                    <v-btn x-small color="primary" dark v-if="item.lastAccess"
                                           :title="formatDate(item.lastAccess)">
                                        {{ timeSince(item.lastAccess) }}
                                    </v-btn>
                                    <v-btn x-small color="gray" dark v-else>
                                        NEVER
                                    </v-btn>
                                </template>
                            </v-data-table>
                            <v-card-actions v-if="isTLSEnabled">
                                <v-btn color="secondary" :loading="certificatesFetching" dark small
                                       @click="fetchCertificates">
                                    <v-icon left>mdi-table-refresh</v-icon>
                                    Refresh
                                </v-btn>
                                <v-btn color="secondary" :loading="certificatesFetching" dark small
                                       @click="issueCertificate">
                                    <v-icon left>mdi-certificate</v-icon>
                                    Issue new X509 certificate
                                </v-btn>
                            </v-card-actions>
                            <v-divider v-if="isTLSEnabled"/>
                            <!-- END SECTION: mTLS -->

                            <!-- SECTION: API KEY -->
                            <v-card-subtitle>
                                API Token
                            </v-card-subtitle>
                            <v-card-text>
                                <v-text-field
                                        prepend-icon="mdi-refresh"
                                        append-icon="mdi-delete-empty-outline"
                                        v-model="apiToken"
                                        readonly
                                        :hint="apiToken.length > 0 ? 'Copy the token to a secure place, it wont be displayed again.' : ''"
                                        @click:append="revokeAPIToken"
                                        @click:prepend="requestAPIToken">
                                </v-text-field>
                            </v-card-text>
                            <!-- END SECTION: API KEY -->

                            <v-divider/>
                            <v-card-actions>
                                <v-btn color="orange" to="/" small dark elevation="4">
                                    <v-icon left>mdi-arrow-left</v-icon>
                                    to Profile
                                </v-btn>
                            </v-card-actions>
                        </v-card>
                    </v-col>
                </v-row>
            </v-container>
        </v-content>
    </v-auth-app>
</template>

<script>
    import VAuthApp from "../components/VAuthApp";
    import axios from 'axios';
    import moment from 'moment'
    import FileSaver from 'file-saver';
    import sw from 'sweetalert2';

    export default {
        components: {VAuthApp},
        computed: {
            currentUsername() {
                return this.$store.state.username;
            },
            isTLSEnabled() {
                let isEnabled = localStorage.getItem('mTLSEnabled');

                if (isEnabled == null) {
                    return this.serverInfo.mTLSEnabled;
                } else {
                    return isEnabled === 'true';
                }
            }
        },
        data: () => ({
            certificates: [],

            certificateTableHeaders: [
                {text: 'Name', value: 'name'},
                {text: 'Serial', value: 'serial'},
                {text: 'Issued at', value: 'issuedAt'},
                {text: 'Revoked at', value: 'revokedAt'},
                {text: 'Last access', value: 'lastAccess'},
                {text: 'Actions', value: 'actions'},
            ],

            certificatesFetching: false,

            serverInfo: {
                mTLSEnabled: false,
            },

            apiToken: '',

            otpEnabled: false,
        }),
        methods: {
            revokeCertificate(serial) {
                axios.delete('/api/session/me/certificate/' + serial).then(() => {
                    this.fetchCertificates();
                });
            },
            fetchCertificates() {
                this.certificatesFetching = true;
                axios.get('/api/session/me/certificates').then(r => {
                    this.certificates = r.data;
                }).finally(() => {
                    this.certificatesFetching = false;
                });
            },
            timeSince(date) {
                return moment(date, "YYYY-MM-DD[T]hh:mm:ss").fromNow();
            },
            formatDate(date) {
                return moment(date, "YYYY-MM-DD[T]hh:mm:ss").format('LLL');
            },
            fetchServerInfo() {
                axios.get('/api/ui/info').then(r => {
                    this.serverInfo.mTLSEnabled = r.data.mTLSEnabled;

                    localStorage.setItem('mTLSEnabled', r.data.mTLSEnabled ? 'true' : 'false');
                })
            },
            issueCertificate() {
                sw.fire({
                    title: 'X509 Certificate Name',
                    input: 'text',
                    showCancelButton: true,
                }).then(r => {
                    if (r.value && r.value.length > 0) {
                        this.certificatesFetching = true;
                        axios({
                            url: '/api/session/me/certificates?deviceName=' + encodeURIComponent(r.value),
                            method: 'POST',
                            responseType: 'blob'
                        }).then(r => {
                            FileSaver.saveAs(new Blob([r.data]), this.currentUsername + ".pfx");
                        }).catch(() => {
                            sw.fire({
                                type: 'error',
                                title: 'Error',
                                html: 'Failed to issue new certificate.<br/>If this issue persist, contact a system administrator.'
                            })
                        }).finally(() => {
                            this.fetchCertificates();
                        })
                    }
                })
            },
            fetchProfile() {
                axios.get('/api/session/me').then(r => {
                    this.otpEnabled = r.data.otpEnabled
                });
            },
            disableTwoFactor() {
                axios.delete('/api/session/me/otp').then(() => {
                    this.fetchProfile();
                    sw.fire({title: '2FA disabled!', type: 'error'});
                });
            },
            enableTwoFactor() {
                axios.post('/api/session/me/otp').then(() => {
                    sw.fire({
                        title: 'Verify OTP',
                        html: '<img width="178px" src="/api/session/me/otp/image?cache=' + (new Date()).getTime() + '"/>',
                        input: 'number',
                        showCancelButton: true,
                        confirmButtonText: 'Verify',
                    }).then(r => {
                        if (r.value) {
                            axios.post('/api/session/me/otp/verify', {
                                securityPassword: r.value,
                            }).then(() => {
                                sw.fire('2FA Enabled!');
                                this.fetchProfile();
                            }).catch(() => {
                                this.enableTwoFactor();
                            })
                        }
                    })
                });
            },
            changePassword() {
                sw.fire({
                    title: 'New Password',
                    input: 'password',
                    inputValidator(inputValue) {
                        if (inputValue.length < 8) return 'Password is not secure enough!';
                    },
                    showCancelButton: true,
                }).then(r => {
                    if (r.value) {
                        let password = r.value;

                        sw.fire({
                            title: 'New password verification',
                            input: 'password',
                            inputValidator(inputValue) {
                                if (inputValue !== password) return 'Passwords do not match!';
                            },
                            showCancelButton: true,
                        }).then(() => {
                            axios.patch('/api/session/me', {
                                password: password
                            }).then(() => {
                                sw.fire({
                                    type: 'success',
                                    title: 'Done!',
                                    html: 'Password has been changed.'
                                });
                            }).catch(() => {
                                sw.fire({
                                    type: 'error',
                                    title: 'Failed to update field password',
                                    html: 'If this issue persist, contact a admin about this.'
                                });
                            })
                        })
                    }
                })
            },
            requestAPIToken() {
                axios.post('/api/session/me/api-token').then(r => {
                    this.apiToken = r.data;
                }).catch(() => {
                    sw.fire({
                        type: 'error',
                        title: 'Error',
                        html: 'Failed to delete api token.<br/>If this issue persist, contact a system admin.'
                    })
                })
            },
            revokeAPIToken() {
                axios.delete('/api/session/me/api-token').then(() => {
                    this.apiToken = '';
                }).catch(() => {
                    sw.fire({
                        type: 'error',
                        title: 'Error',
                        html: 'Failed to delete api token.<br/>If this issue persist, contact a system admin.'
                    })
                })
            },
        },
        mounted() {
            this.fetchProfile();
            this.fetchCertificates();
            this.fetchServerInfo();
        }
    }
</script>