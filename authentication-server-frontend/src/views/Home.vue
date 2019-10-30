<template>
    <basic-app location="Home">
        <v-content>
            <v-container>
                <v-row>
                    <v-col>
                        <v-card>
                            <v-toolbar dense color="primary" dark>
                                <v-toolbar-title>
                                    <v-icon class="mb-1">mdi-account</v-icon>
                                    My Profile
                                </v-toolbar-title>
                                <v-spacer></v-spacer>
                                <v-btn icon :loading="isApiRefreshing" @click="getCurrentUser(true)">
                                    <v-icon>mdi-reload</v-icon>
                                </v-btn>
                            </v-toolbar>
                            <v-card-text>
                                <v-row>
                                    <v-col cols="12" lg="6">
                                        <v-text-field v-model="user.username" disabled
                                                      prepend-inner-icon="mdi-account-badge"
                                                      suffix="Username"></v-text-field>
                                    </v-col>
                                    <v-col cols="12" lg="6">
                                        <v-text-field v-model="user.email"
                                                      prepend-inner-icon="mdi-email-newsletter"
                                                      suffix="E-Mail"></v-text-field>
                                    </v-col>
                                    <v-col cols="12" lg="6">
                                        <v-text-field v-model="user.displayName"
                                                      prepend-inner-icon="mdi-account-card-details-outline"
                                                      suffix="Display-Name"></v-text-field>
                                    </v-col>
                                </v-row>
                                <v-row v-if="user.admin">
                                    <v-col cols="12" lg="6">
                                        <v-text-field v-model="apiToken" readonly :loading="isApiRefreshing"
                                                      prepend-inner-icon="mdi-api"
                                                      suffix="API Token"
                                                      append-outer-icon="mdi-reload"
                                                      @click:append-outer="requestApiToken()"></v-text-field>
                                    </v-col>
                                </v-row>
                            </v-card-text>
                            <v-card-text v-if="!user.otpEnabled">
                                <v-alert color="error" dense type="error" elevation="4">
                                    You'r Account is not secured with
                                    <v-icon class="pb-1">mdi-two-factor-authentication</v-icon>
                                    !<br>
                                    This is highly recommended!
                                </v-alert>
                            </v-card-text>
                            <v-card-actions>
                                <v-btn small color="success" :loading="isApiRefreshing" @click="saveUpdateForm()">
                                    <v-icon small left>mdi-content-save-all</v-icon>
                                    Save
                                </v-btn>
                                <v-btn small dark color="blue-grey" @click="changePassword()">
                                    <v-icon small left>mdi-lock-reset</v-icon>
                                    Change Password
                                </v-btn>
                                <v-btn v-if="user.otpEnabled" small color="error" @click="disableTwoFactor()">
                                    <v-icon small left>mdi-two-factor-authentication</v-icon>
                                    Disable
                                </v-btn>
                                <v-btn v-if="!user.otpEnabled" small color="success" @click="enableTwoFactor()">
                                    <v-icon small left>mdi-two-factor-authentication</v-icon>
                                    Enable
                                </v-btn>
                            </v-card-actions>
                        </v-card>
                    </v-col>
                </v-row>
            </v-container>
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
                sweetalert2.fire({
                    type: 'warning',
                    title: 'Are you sure?',
                    text: 'If you wanna really disable 2FA, press OK',
                    showCancelButton: true,
                    cancelButtonText: 'Abort',
                    confirmButtonText: 'Agree',
                }).then(r => {
                    if (r.value) {
                        this.updateLocalUser({otpSecret: null});
                    }
                });
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