<template>
    <v-app>
        <v-content>
            <v-parallax height="100%" src="https://images.unsplash.com/photo-1498050108023-c5249f4df085?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1952&q=80"
                        class="fill-height">
                <v-container>
                    <v-layout align-center="align-center" justify-center="justify-center">
                        <v-flex class="text-center" style="max-width: 500px">
                            <div class="display-1 mb-3">
                                <v-icon class="mr-2 mb-2" dark large>fas fa-key</v-icon>
                                Authenticate
                            </div>
                            <v-card light>
                                <v-card-text>
                                    <div class="subheading text-center">
                                        Sign in
                                    </div>
                                    <v-alert class="ma-2" v-if="success" type="success" border="left" colored-border>
                                        Login Success!
                                    </v-alert>
                                    <div v-if="!success">
                                        <v-text-field ref="txtUsername"
                                                v-if="!otpRequired"
                                                :error="loginError"
                                                v-model="username"
                                                @keydown.enter="auth"
                                                light
                                                prepend-icon="fas fa-user mr-2 mt-2"
                                                label="Name">
                                        </v-text-field>
                                        <v-text-field
                                                v-if="!otpRequired"
                                                :error="loginError"
                                                :error-messages="loginErrorText"
                                                @keydown.enter="auth"
                                                v-model="password" type="password" light="light"
                                                prepend-icon="fas fa-key mr-2 mt-2"
                                                label="Password">
                                        </v-text-field>
                                        <v-expand-transition>
                                            <v-text-field ref="txtOtpCode"
                                                          v-if="otpRequired"
                                                          :error="loginError"
                                                    :error-messages="loginErrorText"
                                                    @keydown.enter="auth"
                                                    v-model="otpCode" type="number" light="light"
                                                    prepend-icon="fas fa-key mr-2 mt-2" counter="6"
                                                    label="One-Time-Password">
                                            </v-text-field>
                                        </v-expand-transition>
                                        <br>
                                        <v-divider/>
                                        <br>
                                        <v-row>
                                            <v-col>
                                                <v-btn :loading="processing" color="indigo" dark @click.prevent="auth" block
                                                       type="button">
                                                    <v-icon small left>fas fa-sign-in-alt</v-icon>
                                                    Sign in
                                                </v-btn>
                                            </v-col>

                                            <v-col v-if="otpRequired">
                                                <v-btn color="warning" dark @click.prevent="abortAuth" block
                                                       type="button">
                                                    <v-icon small left>fas fa-times</v-icon>
                                                    Abort
                                                </v-btn>
                                            </v-col>
                                        </v-row>
                                    </div>
                                </v-card-text>
                            </v-card>
                        </v-flex>
                    </v-layout>
                </v-container>
            </v-parallax>
        </v-content>

        <Footer/>
    </v-app>
</template>

<script>
    import axios from "axios";
    import Footer from "../components/Footer";

    export default {
        components: {Footer},
        data      : () => ({
            username        : '',
            password        : '',
            otpCode         : '',
            otpRequired     : false,
            processing      : false,
            loginError      : false,
            loginErrorText  : '',
            loginColor      : 'indigo',
            success         : false,
            notAuthenticated: false,
        }),
        methods   : {
            abortAuth() {
                this.username = '';
                this.password = '';
                this.otpRequired = false;
                this.otpCode = '';
                this.processing     = false;
                this.loginError     = false;
                this.loginColor     = 'indigo';
                this.loginErrorText = '';

                this.$nextTick(() => {
                    this.$refs.txtUsername.focus();
                });
            },
            auth() {
                this.processing     = true;
                this.loginError     = false;
                this.loginColor     = 'indigo';
                this.loginErrorText = '';

                setTimeout(() => {
                    axios.post('/cas/login?service=' + this.$route.query.service, {
                        username: this.username,
                        password: this.password,
                        totp    : this.otpCode,
                        'no-cas': this.cas ? 'false' : 'true',
                    }, {timeout: 10000}).then((response) => {
                        this.processing = false;
                        this.success    = true;
                        this.loginColor = 'success';

                        if (this.cas) {
                            setTimeout(() => location.replace(response.data.location), 1000);
                        } else {
                            if (this.$route.query.service) {
                                setTimeout(() => location.replace(this.$route.query.service), 2000);
                            }
                        }
                    }).catch((err) => {
                        this.processing = false;

                        if (!err.response) {
                            this.loginError     = true;
                            this.loginColor     = 'error';
                            this.loginErrorText = err.toString();
                        } else {
                            if (err.response.status === 409) {
                                this.otpRequired = true;
                                this.$nextTick(() => this.$refs.txtOtpCode.focus());
                            } else {
                                this.processing     = false;
                                this.loginError     = true;
                                this.loginColor     = 'error';
                                this.loginErrorText = err.response.data.message ? err.response.data.message : err.toString();
                            }
                        }
                    })
                }, 1250);
            },
            check() {
                axios.post('/api/validate').catch((err) => {
                    if (err.response.status === 403) {
                        this.notAuthenticated = true;
                    }
                })
            },
            signOut() {
                this.processing = true;
                axios.get('/api/deauth').then(() => {
                    this.notAuthenticated = true;
                    this.username         = '';
                    this.password         = '';
                    this.success          = false;
                    this.processing       = false;
                }).catch(() => {
                    this.processing = false;
                })
            }
        },
        mounted() {
            this.check();
        },
        props     : ['cas']
    }
</script>