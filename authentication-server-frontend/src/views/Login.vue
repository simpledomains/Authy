<!--suppress JSUnresolvedVariable, JSUnresolvedFunction -->
<template>
    <v-app>
        <v-layout align-center="align-center" justify-center="justify-center" class="pl-3 pr-3">
            <v-flex class="text-center" style="max-width: 500px">
                <v-card raised>
                    <v-card-title class="justify-center" dense color="primary">
                        <v-icon color="primary" left>mdi-key</v-icon>
                        Sign in
                    </v-card-title>
                    <v-card-text>
                        <v-alert class="ma-2" icon="mdi-key-star" v-if="status === ST.LOGIN_SUCCESS" type="success"
                                 border="bottom">
                            Login Success!
                        </v-alert>
                        <div v-if="status !== ST.LOGIN_SUCCESS">
                            <v-text-field ref="txtUsername"
                                          v-if="formStatus !== ST.SECURITY_TOKEN_REQUIRED"
                                          :error="!!errorCode"
                                          v-model="username"
                                          @keydown.enter="auth"
                                          prepend-icon="mdi-account"
                                          label="Name">
                            </v-text-field>
                            <v-text-field
                                    v-if="formStatus !== ST.SECURITY_TOKEN_REQUIRED"
                                    :error="!!errorCode"
                                    :error-messages="errorCode"
                                    @keydown.enter="auth"
                                    v-model="password" type="password"
                                    prepend-icon="mdi-textbox-password"
                                    label="Password">
                            </v-text-field>
                            <v-expand-transition>
                                <v-text-field ref="txtOtpCode"
                                              v-if="formStatus === ST.SECURITY_TOKEN_REQUIRED"
                                              :error="!!errorCode"
                                              :error-messages="errorCode"
                                              @keydown.enter="auth"
                                              v-model="securityPassword" type="number"
                                              prepend-icon="mdi-textbox-password" counter="6"
                                              label="One-Time-Password">
                                </v-text-field>
                            </v-expand-transition>
                            <br>
                            <v-divider/>
                            <br>
                            <v-row>
                                <v-col>
                                    <v-btn :loading="status === ST.PROCESSING" color="primary" dark
                                           @click.prevent="auth" block
                                           type="button">
                                        Sign in &nbsp;
                                        <v-icon>mdi-login</v-icon>
                                    </v-btn>
                                </v-col>

                                <v-col v-if="formStatus === ST.SECURITY_TOKEN_REQUIRED">
                                    <v-btn color="warning" dark @click.prevent="abortAuth" block
                                           type="button">
                                        <v-icon small left>fas fa-times</v-icon>
                                        Abort
                                    </v-btn>
                                </v-col>

                                <!--
                                <v-col v-if="publicKeyCredentialEnabled">
                                    <v-btn :loading="status === ST.WEBAUTHN" color="primary" dark
                                           @click.prevent="startWebAuthN" block
                                           type="button">
                                        Sign in (beta)
                                        <v-icon>mdi-fingerprint</v-icon>
                                    </v-btn>
                                </v-col>
                                -->
                            </v-row>
                        </div>
                    </v-card-text>
                </v-card>
            </v-flex>
        </v-layout>

        <Footer/>
    </v-app>
</template>

<script>
    import axios from "axios";
    import Footer from "../components/Footer";
    import {get} from '@github/webauthn-json';
    import {fromByteArray, toByteArray} from 'base64-js';

    export default {
        props: ['cas'],
        components: {Footer},
        data: () => ({

            ST: {
                IDLE: 1,
                NOT_AUTHENTICATED: 1,
                LOGIN_SUCCESS: 2,
                LOGIN_FAILED: 3,
                SECURITY_TOKEN_REQUIRED: 4,
                PROCESSING: 999,
                WEBAUTHN: 998,
            },

            formStatus: 1,
            status: 1,
            errorCode: null,

            username: '',
            password: '',

            securityPassword: '',

            publicKeyCredentialEnabled: window.PublicKeyCredential,
        }),
        methods: {
            /*
            async startWebAuthN() {
                this.setStatus(this.ST.WEBAUTHN);
                try {
                    let start = await axios.post('/webauthn/assertion/start?username=' + this.username);
                    let options = start.data.publicKeyCredentialRequestOptions;
                    options.allowCredentials = options.allowCredentials || [];

                    let credential = await get({
                        publicKey: options
                    });

                    credential.clientExtensionResults = assertion.getClientExtensionResults();

                    axios.post('/webauthn/assertion/finish', credential, {
                        withCredentials: true
                    }).then(r => {
                        window.location.href = '/';
                    })
                } catch (e) {
                    console.log("Something went wrong doing webauthn", e);
                    this.setStatus(this.ST.LOGIN_FAILED);
                    this.errorCode = e.message;
                }
                this.setStatus(this.ST.IDLE);
            },*/
            setStatus(code, form) {
                this.status = code;

                if (form)
                    this.formStatus = form;
            },
            abortAuth() {
                this.setStatus(this.ST.NOT_AUTHENTICATED, this.ST.IDLE);
                this.password = '';
                this.securityPassword = '';
            },
            auth() {
                this.setStatus(this.ST.PROCESSING);

                setTimeout(() => {
                    axios.post('/cas/login?service=' + this.$route.query.service, {
                        username: this.username,
                        password: this.password,
                        securityPassword: this.securityPassword.length > 0 ? this.securityPassword : null,
                        cas: this.cas ? 'true' : 'false',
                    }, {timeout: 10000}).then(response => {
                        this.setStatus(this.ST.LOGIN_SUCCESS);
                        localStorage.removeItem('AuthyUser');

                        if (this.cas) {
                            setTimeout(() => location.replace(response.data.location), 1250);
                        } else {
                            if (this.$route.query.service) {
                                setTimeout(() => location.replace(this.$route.query.service), 1250);
                            }
                        }
                    }).catch(err => {
                        // CATCH: OTP Required Error (HTTP 409)
                        if (err.response && err.response.status === 409) {
                            this.setStatus(this.ST.SECURITY_TOKEN_REQUIRED, this.ST.SECURITY_TOKEN_REQUIRED);

                            if (err.response.data.message) {
                                this.errorCode = err.response.data.message;
                            }
                        } else {
                            throw err;
                        }
                    }).catch(err => {
                        // CATCH: any other error
                        if (err.response) {
                            this.setStatus(this.ST.LOGIN_FAILED, this.ST.IDLE);
                            this.errorCode = err.response.data.message ? err.response.data.message : err.toString();
                        }
                    });
                }, 1250);
            },
            signOut() {
                this.setStatus(this.ST.PROCESSING, this.ST.IDLE);
                axios.get('/cas/logout').then(() => {
                    this.username = '';
                    this.password = '';
                    this.securityPassword = '';
                    this.setStatus(this.ST.IDLE, this.ST.IDLE);

                    localStorage.removeItem('AuthyUser');
                }).catch(() => {
                    this.setStatus(this.ST.IDLE, this.ST.IDLE);
                })
            }
        }
    }
</script>