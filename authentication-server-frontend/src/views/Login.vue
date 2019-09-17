<template>
    <v-app>
        <v-layout align-center="align-center" justify-center="justify-center">
            <v-flex class="text-center" style="max-width: 500px">
                <v-card raised>
                    <v-card-title class="justify-center" dense
                                  style="background-color: #3f51b5 !important; color: white;">
                        Sign in
                    </v-card-title>
                    <v-card-text>
                        <v-alert class="ma-2" v-if="status === statusTypes.LOGIN_SUCCESS" type="success" border="left"
                                 colored-border>
                            Login Success!
                        </v-alert>
                        <div v-if="status !== statusTypes.LOGIN_SUCCESS">
                            <v-text-field ref="txtUsername"
                                          v-if="status !== statusTypes.SECURITY_TOKEN_REQUIRED"
                                          :error="errorCode"
                                          v-model="username"
                                          @keydown.enter="auth"
                                          prepend-icon="mdi-account"
                                          label="Name">
                            </v-text-field>
                            <v-text-field
                                    v-if="status !== statusTypes.SECURITY_TOKEN_REQUIRED"
                                    :error="errorCode"
                                    :error-messages="errorCode"
                                    @keydown.enter="auth"
                                    v-model="password" type="password"
                                    prepend-icon="mdi-textbox-password"
                                    label="Password">
                            </v-text-field>
                            <v-expand-transition>
                                <v-text-field ref="txtOtpCode"
                                              v-if="status === statusTypes.SECURITY_TOKEN_REQUIRED"
                                              :error="errorCode"
                                              :error-messages="errorCode"
                                              @keydown.enter="auth"
                                              v-model="securityToken" type="number"
                                              prepend-icon="mdi-textbox-password" counter="6"
                                              label="One-Time-Password">
                                </v-text-field>
                            </v-expand-transition>
                            <br>
                            <v-divider/>
                            <br>
                            <v-row>
                                <v-col>
                                    <v-btn :loading="status === statusTypes.PROCESSING" color="indigo" dark
                                           @click.prevent="auth" block
                                           type="button">
                                        <v-icon small left>fas fa-sign-in-alt</v-icon>
                                        Sign in
                                    </v-btn>
                                </v-col>

                                <v-col v-if="status === statusTypes.SECURITY_TOKEN_REQUIRED">
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

            statusTypes: {
                IDLE: 1,
                NOT_AUTHENTICATED: 1,
                LOGIN_SUCCESS: 2,
                LOGIN_FAILED: 3,
                SECURITY_TOKEN_REQUIRED: 4,
                PROCESSING: 999,
            },

            status: 1,
            errorCode: null,

            username: '',
            password: '',

            securityToken: '',
        }),
        methods: {
            setStatus(code) {
                this.status = code;
            },
            abortAuth() {
                this.setStatus(this.statusTypes.NOT_AUTHENTICATED);
                this.password = '';
                this.securityToken = '';
            },
            auth() {
                this.setStatus(this.statusTypes.PROCESSING);

                axios.post('/cas/login?service=' + this.$route.query.service, {
                    username: this.username,
                    password: this.password,
                    securityToken: this.securityToken,
                    cas: this.cas ? 'true' : 'false',
                }, {timeout: 10000}).then(response => {
                    this.setStatus(this.statusTypes.LOGIN_SUCCESS);

                    if (this.cas) {
                        setTimeout(() => location.replace(response.data.location), 1000);
                    } else {
                        if (this.$route.query.service) {
                            setTimeout(() => location.replace(this.$route.query.service), 2000);
                        }
                    }
                }).catch(err => {
                    // CATCH: OTP Required Error (HTTP 409)
                    if (err.response && err.response.statusCode === 409) {
                        this.status = this.statusTypes.SECURITY_TOKEN_REQUIRED;
                    } else {
                        throw err;
                    }
                }).catch(err => {
                    // CATCH: any other error
                    if (err.response) {
                        this.setStatus(this.statusTypes.LOGIN_FAILED);
                        this.errorCode = err.response.data.message ? err.response.data.message : err.toString();
                    }
                });
            },
            signOut() {
                this.setStatus(this.statusTypes.PROCESSING);
                axios.get('/cas/logout').then(() => {
                    this.username = '';
                    this.password = '';
                    this.securityToken = '';
                    this.setStatus(this.statusTypes.IDLE);
                }).catch(() => {
                    this.setStatus(this.statusTypes.IDLE);
                })
            }
        }
    }
</script>