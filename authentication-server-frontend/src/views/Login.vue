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
                        <v-alert class="ma-2" elevation="4" icon="mdi-key-star" type="success"
                                 v-if="form_states.success">
                            Login Success!
                        </v-alert>
                        <div v-if="!form_states.success">
                            <v-text-field ref="txtUsername"
                                          v-if="!form_states.requireOtp"
                                          :error="!!errorCode"
                                          v-model="username"
                                          @keydown.enter="auth"
                                          prepend-icon="mdi-account"
                                          label="Name">
                            </v-text-field>
                            <v-text-field
                                    v-if="!form_states.requireOtp"
                                    :error="!!errorCode"
                                    :error-messages="errorCode"
                                    @keydown.enter="auth"
                                    v-model="password" type="password"
                                    prepend-icon="mdi-textbox-password"
                                    label="Password">
                            </v-text-field>
                            <v-expand-transition>
                                <v-text-field ref="txtOtpCode"
                                              v-if="form_states.requireOtp"
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
                                    <v-btn :loading="form_states.isProcessing" color="primary" dark
                                           @click.prevent="auth" block
                                           type="button">
                                        Sign in &nbsp;
                                        <v-icon>mdi-login</v-icon>
                                    </v-btn>
                                </v-col>

                                <v-col v-if="form_states.requireOtp">
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

        <Footer/>
    </v-app>
</template>

<script>
    import axios from "axios";
    import Footer from "../components/Footer";

    export default {
        props: ['cas'],
        components: {Footer},
        data: () => ({
            username: '',
            password: '',
            securityPassword: '',

            errorCode: "",

            form_states: {
                requireOtp: false,
                isLoginProcess: false,
                isProcessing: false,
                success: false,
            }
        }),
        methods: {
            abortAuth() {
                this.form_states.requireOtp = false;
                this.form_states.isLoginProcess = false;
                this.form_states.isProcessing = false;
                this.form_states.success = false;

                this.securityPassword = '';
                this.username = '';
                this.password = '';

                setTimeout(() => this.$refs.txtUsername.focus(), 200);
            },
            isRemoteAuthenticated() {
                axios.get('/api/session/me').then(r => {
                    this.$store.commit('setAuthData', r.data);
                    this.$router.push('/');
                })
            },
            auth() {
                this.form_states.isProcessing = true;
                this.errorCode = "";

                axios.post('/cas/login?service=' + this.$route.query.service, {
                    username: this.username,
                    password: this.password,
                    securityPassword: this.securityPassword.length > 0 ? this.securityPassword : null,
                    cas: this.cas ? 'true' : 'false',
                }).then(response => {
                    this.form_states.success = true;

                    if (response.data.token) {
                        this.$store.commit('setToken', response.data.token);
                    }

                    if (this.cas) {
                        setTimeout(() => location.replace(response.data.location), 1250);
                    } else {
                        if (this.$route.query.service) {
                            setTimeout(() => this.$router.push(this.$route.query.service), 1250);
                        }
                    }

                }).catch(e => {
                    if (e.response.status === 409) {
                        this.form_states.requireOtp = true;

                        setTimeout(() => {
                            this.$refs.txtOtpCode.focus();
                        }, 200);
                    } else if (e.response.status === 401) {
                        let code = e.response.data.errorCode;

                        if (code === 'USER_ACCOUNT_BLOCKED') {
                            this.errorCode = 'Too many tries with wrong credentials. Try again later.';
                        } else {
                            this.errorCode = "Username or Password does not match.";
                        }
                    }
                }).finally(() => {
                    this.form_states.isProcessing = false;
                })
            }
        },
        mounted() {
            this.isRemoteAuthenticated();
            this.$refs.txtUsername.focus();
        }
    }
</script>