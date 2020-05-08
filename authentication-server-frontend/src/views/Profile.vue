<template>
    <v-auth-app location="My Profile">
        <v-content>
            <v-container>
                <v-row>
                    <v-col>
                        <v-alert color="indigo" dark type="info" elevation="8">
                            Welcome {{ username }}! This is your Authy dashboard! You can change your personal data for
                            authentication here.
                        </v-alert>
                    </v-col>
                </v-row>
                <v-row>
                    <v-col>
                        <v-alert type="error" color="error" dense v-if="!otpEnabled && !fetchingProfile" elevation="3">
                            <v-icon left>mdi-two-factor-authentication</v-icon>
                            is not enabled!
                        </v-alert>
                        <v-card outlined elevation="1" :loading="fetchingProfile">
                            <v-card-title>My Profile</v-card-title>
                            <v-card-subtitle>You can customize your profile here.</v-card-subtitle>
                            <v-card-text>
                                <v-row>
                                    <v-col cols="12" md="6">
                                        <v-text-field disabled v-model="username" label="Username"
                                                      prepend-icon="mdi-form-textbox"/>
                                    </v-col>
                                    <v-col cols="12" md="6">
                                        <v-update-text-field
                                                type="text"
                                                :initial-value="displayName"
                                                object-key="displayName"
                                                label="Display name"
                                                icon="mdi-form-textbox"/>
                                    </v-col>
                                    <v-col cols="12" md="3">
                                        <v-text-field type="password" v-model="password"
                                                      label="Password (fill to change)"
                                                      prepend-icon="mdi-key" @keydown.enter="changePassword"
                                                      :error="!!passwordError"
                                                      :error-messages="passwordError"/>
                                    </v-col>
                                    <v-col cols="12" md="3">
                                        <v-text-field type="password" v-model="password2" label="Repeat password"
                                                      prepend-icon="mdi-key" @keydown.enter="changePassword"
                                                      :error="!!passwordError"
                                                      :error-messages="passwordError"/>
                                    </v-col>
                                    <v-col cols="12" md="6">
                                        <v-update-text-field
                                                type="text"
                                                :initial-value="email"
                                                object-key="email"
                                                label="E-Mail"
                                                icon="mdi-at"/>
                                    </v-col>
                                </v-row>
                            </v-card-text>
                            <v-card-actions>
                                <v-btn small color="error" v-if="password.length > 0">
                                    <v-icon left>mdi-content-save</v-icon>
                                    Change password *
                                </v-btn>
                                <v-spacer/>
                                <v-btn small color="orange" dark to="/profile/security" elevation="4">
                                    <v-icon left>mdi-arrow-right</v-icon>
                                    Security
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
    import axios from 'axios'
    import VUpdateTextField from "../components/VUpdateTextField";

    export default {
        components: {VUpdateTextField, VAuthApp},
        computed: {
            username() {
                return this.$store.state.username
            }
        },
        data: () => ({
            otpEnabled: false,
            fetchingProfile: true,

            username: '',
            password: '',
            password2: '',
            email: '',
            displayName: '',

            passwordError: '',
        }),
        methods: {
            changePassword() {
                this.passwordError = '';

                if (this.password.length >= 8 && this.password === this.password2) {
                    axios.patch('/api/session/me', {
                        password: this.password,
                    }).then(() => {
                        this.password = '';
                        this.password2 = '';
                    })
                } else {
                    this.passwordError = 'Password is not strong enough or do not match.';
                }
            },
            fetchProfile() {
                axios.get('/api/session/me').then(r => {
                    this.username = r.data.username;
                    this.displayName = r.data.displayName;
                    this.email = r.data.email;
                    this.otpEnabled = r.data.otpEnabled
                }).finally(() => {
                    this.fetchingProfile = false;
                })
            }
        },
        mounted() {
            this.fetchProfile();
        }
    }
</script>