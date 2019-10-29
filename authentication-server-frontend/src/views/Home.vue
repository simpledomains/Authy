<template>
    <basic-app>
        <v-content class="ml-3 mr-3">
            <v-row>
                <v-col lg="6" offset-lg="3" sm="12" offset-sm="0" md="12" offset-md="0">
                    <v-card>
                        <v-toolbar dense color="primary" dark elevation="1" tile>
                            <v-toolbar-title>
                                <v-icon v-if="!user.admin">mdi-account</v-icon>
                                <v-icon v-if="user.admin">mdi-shield-account</v-icon>
                                My Profile
                            </v-toolbar-title>
                        </v-toolbar>
                        <v-card-text v-if="user != null">
                            <v-row>
                                <v-col cols="12">
                                    <v-alert type="success" color="success" border="bottom" v-if="user.otpEnabled">
                                        You'r Account is secured by 2-FA
                                    </v-alert>
                                    <v-alert type="error" color="error" border="bottom" v-else>
                                        You'r Account is not secured with 2-FA!
                                    </v-alert>
                                </v-col>
                            </v-row>
                            <v-row>
                                <v-col cols="6">
                                    <v-text-field
                                            v-model="user.username"
                                            :counter="24"
                                            label="Username"
                                            disabled
                                            required>
                                    </v-text-field>
                                </v-col>
                                <v-col cols="6">
                                    <v-text-field
                                            v-model="user.displayName"
                                            :counter="24"
                                            label="Display name"
                                            required>
                                    </v-text-field>
                                </v-col>
                                <v-col cols="6">
                                    <v-text-field
                                            v-model="user.email"
                                            :counter="32"
                                            label="E-Mail Address"
                                            required>
                                    </v-text-field>
                                </v-col>
                            </v-row>
                            <v-row v-if="user.admin" class="d-none d-sm-flex">
                                <v-col cols="10">
                                    <v-text-field readonly label="API Token (only visible on first request)"
                                                  v-model="apiToken"></v-text-field>
                                </v-col>
                                <v-col cols="2" justify="center" align-self="center">
                                    <v-btn elevation="1" dark fab small color="deep-orange" :loading="isApiRefreshing"
                                           @click="requestApiToken">
                                        <v-icon>mdi-refresh</v-icon>
                                    </v-btn>
                                </v-col>
                            </v-row>
                        </v-card-text>
                        <v-divider/>
                        <v-card-actions>
                            <v-btn elevation="1" color="indigo" dark @click="saveUpdateForm()">
                                <v-icon>mdi-content-save-outline</v-icon>&nbsp; Save
                            </v-btn>

                            <v-btn elevation="1" color="warning" dark @click="changePassword()">
                                <v-icon>mdi-key-star</v-icon>&nbsp; Change Password
                            </v-btn>

                            <v-btn elevation="1" color="success" dark @click="enableTwoFactor()"
                                   v-if="!user.otpEnabled">
                                Enable &nbsp;
                                <v-icon>mdi-two-factor-authentication</v-icon>
                            </v-btn>

                            <!--
                            <v-btn elevation="1" color="success" dark @click="registerU2FDevice()">
                                Register Authenticator &nbsp;
                                <v-icon>mdi-shield-account-outline</v-icon>
                            </v-btn>
                            -->

                            <v-btn elevation="1" color="error" dark @click="disableTwoFactor()" v-if="user.otpEnabled">
                                Disable &nbsp;
                                <v-icon>mdi-two-factor-authentication</v-icon>
                            </v-btn>

                            <v-btn elevation="1" color="blue-grey" dark @click="getCurrentUser(true)"
                                   class="d-none d-md-flex">
                                <v-icon>mdi-account-convert</v-icon>&nbsp; Refresh
                            </v-btn>
                        </v-card-actions>
                    </v-card>
                </v-col>
            </v-row>
        </v-content>
    </basic-app>
</template>

<style>
    body {
        font-family: 'DejaVu Sans', Arial, Helvetica, sans-serif;
    }
</style>

<script>
    import axios from "axios";
    import sweetalert2 from 'sweetalert2';
    import BasicApp from "../components/BasicApp";
    import {create} from '@github/webauthn-json';

    export default {
        components: {BasicApp},

        data: () => ({
            user: {},
            password: '',
            apiToken: '',
            isApiRefreshing: false,
        }),

        methods: {
            /*
            async registerU2FDevice() {
                let response = await axios.post('/webauthn/registration/start');
                response = response.data;

                let credential = await create({
                    publicKey: response
                });

                try {
                    credential.clientExtensionResults = credential.getClientExtensionResults();
                } catch (e) {
                    credential.clientExtensionResults = {};
                }

                let finish = await axios.post('/webauthn/registration/finish', credential, {responseType: 'text'});
            },*/
            requestApiToken() {
                this.isApiRefreshing = true;

                axios.post('/api/profile/api-token').then(r => {
                    this.apiToken = r.data;
                }).finally(() => {
                    this.isApiRefreshing = false;
                });
            },
            disableTwoFactor() {
                this.updateLocalUser({otpSecret: null});
            },
            enableTwoFactor() {
                axios.get('/api/profile/otp/start').then(() => {
                    sweetalert2.fire({
                        title: 'Verify OTP',
                        html: '<img width="178px" src="/api/profile/otp/qrcode?date=' + (new Date()).getTime() + '"/>',
                        input: 'number',
                        showCancelButton: true,
                        confirmButtonText: 'Verify',
                    }).then(r => {
                        if (r.value) {
                            axios.post('/api/profile/otp/verify', {
                                securityPassword: r.value,
                            }).then(() => {
                                sweetalert2.fire('2FA Enabled!');
                                this.getCurrentUser(true);
                            }).catch(() => {
                                this.enableTwoFactor();
                            })
                        }
                    })
                });
            },
            saveUpdateForm() {
                this.updateLocalUser({
                    email: this.user.email,
                    displayName: this.user.displayName,
                })
            },
            getCurrentUser(force) {
                this.user = JSON.parse(localStorage.getItem('AuthyUser'));

                if (!this.user || force) {
                    axios.get('/api/me').then(r => {
                        localStorage.setItem('AuthyUser', JSON.stringify(r.data));
                        this.user = r.data;
                    }).catch(() => {
                        this.$router.push('/login?service=/');
                    });
                }
            },
            changePassword() {
                sweetalert2.queue([
                    {
                        title: 'Password',
                        text: 'Your new Password',
                        input: 'password',
                        confirmButtonText: 'Next &rarr;',
                        showCancelButton: true,
                        progressSteps: ['1', '2']
                    },
                    {
                        title: 'Repeat Password',
                        text: 'Repeat your new password.',
                        input: 'password',
                        confirmButtonText: 'Check & Save.',
                        showCancelButton: true,
                        progressSteps: ['1', '2']
                    }
                ]).then((result) => {

                    if (result.value[0].length > 6 && result.value[0] === result.value[1]) {
                        sweetalert2.fire({
                            title: 'Perfect!',
                            html:
                                'Your Password was changed!',
                            confirmButtonText: 'Nice!'
                        });

                        axios.patch('/api/me', {
                            password: result.value[0],
                        });
                    } else {
                        sweetalert2.fire({
                            title: 'God damn!',
                            html:
                                'Your passwords do not match or its not as strong as required.',
                            confirmButtonText: 'Ok :('
                        });
                    }
                })
            },
            updateLocalUser(data) {
                axios.patch("/api/me", data).then(() => {
                    sweetalert2.fire('Update Complete!');
                    this.getCurrentUser(true);
                })
            }
        },
        mounted() {
            this.getCurrentUser();
        }
    }
</script>