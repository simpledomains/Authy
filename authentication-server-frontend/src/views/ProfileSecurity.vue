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

                            <v-card-title>mTLS Settings</v-card-title>
                            <v-data-table
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

                            <v-card-actions>
                                <v-btn color="success" :loading="certificatesFetching" dark small
                                       @click="fetchCertificates">
                                    <v-icon left>mdi-table-refresh</v-icon>
                                    Refresh
                                </v-btn>
                                <v-btn color="success" :loading="certificatesFetching" dark small
                                       @click="issueCertificate">
                                    <v-icon left>mdi-certificate</v-icon>
                                    Issue new X509 certificate
                                </v-btn>
                            </v-card-actions>

                            <v-divider/>
                            <v-card-title>2FA Settings</v-card-title>
                            <v-card-text v-if="otpEnabled">
                                <v-btn color="error" dark small @click="disableTwoFactor">
                                    Disable
                                    <v-icon>mdi-two-factor-authentication</v-icon>
                                </v-btn>
                            </v-card-text>
                            <v-card-text v-if="!otpEnabled">
                                <v-btn color="success" dark small @click="enableTwoFactor">
                                    Enable
                                    <v-icon>mdi-two-factor-authentication</v-icon>
                                </v-btn>
                            </v-card-text>

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

            otpEnabled: false,

            certificatesFetching: false,
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

        },
        mounted() {
            this.fetchProfile();
            this.fetchCertificates();
        }
    }
</script>