<template>
    <v-auth-app location="My Profile">
        <v-content>
            <v-container>
                <cas-dismissable-alert id="profile_welcome_notice" color="indigo" dark type="info" elevation="8">
                    Welcome {{ currentUsername }}! This is your Authy dashboard! You can change your personal
                    data for
                    authentication here.
                </cas-dismissable-alert>
                <v-alert type="error" color="error"
                         v-if="!otpEnabled && !fetchingProfile && isFieldEmpty(remoteAuthy)" elevation="8">
                    You'r account is not secured with
                    <v-icon>mdi-two-factor-authentication</v-icon>
                    but its highly recommended to do so.<br/>
                    Go to your
                    <router-link style="text-decoration: underline dotted;color: white;"
                                 to="/profile/security">security settings
                    </router-link>
                    to enable 2FA.
                </v-alert>
                <v-row>
                    <v-col>
                        <v-card outlined elevation="2" :loading="fetchingProfile">
                            <v-card-title>My Profile</v-card-title>
                            <v-card-subtitle>You can customize your profile here.</v-card-subtitle>
                            <v-card-text>
                                <v-row v-if="!fetchingProfile">
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

                            <v-divider/>
                            <v-card-actions>
                                <v-btn small color="orange" dark to="/profile/security" elevation="4">
                                    Security
                                    <v-icon right>mdi-arrow-right</v-icon>
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
    import CasDismissableAlert from "../components/CasDismissableAlert";

    export default {
        components: {CasDismissableAlert, VUpdateTextField, VAuthApp},
        computed: {
            currentUsername() {
                return this.$store.state.username
            }
        },
        data: () => ({
            otpEnabled: false,
            fetchingProfile: true,

            username: '',
            email: '',
            displayName: '',
            remoteAuthy: ''
        }),
        methods: {
            fetchProfile() {
                axios.get('/api/session/me').then(r => {
                    this.username = r.data.username;
                    this.displayName = r.data.displayName;
                    this.email = r.data.email;
                    this.otpEnabled = r.data.otpEnabled
                    this.remoteAuthy = r.data.remoteAuthy
                }).finally(() => {
                    this.fetchingProfile = false;
                })
            },
            isFieldEmpty(field) {
                return field == null || field.trim() === '';
            }
        },
        mounted() {
            this.fetchProfile();
        }
    }
</script>