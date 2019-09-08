<template>
    <v-app v-touch="{right: () => $refs.toolbar.openMenu(), left: () => $refs.toolbar.closeMenu()}">
        <Toolbar ref="toolbar" location="Home" :is-admin="userData.admin" :logout-button="!!userData.name"
                 @sign-out="signOut"/>

        <v-parallax height="100%"
                    src="https://images.unsplash.com/photo-1498050108023-c5249f4df085?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1952&q=80"
                    class="fill-height">
            <v-content>

                <v-container pa-5>
                    <v-row>
                        <v-flex xs12 sm12 lg8 offset-lg2>
                            <v-alert type="error" border="left" v-if="profile === false">
                                This is a statically configured account. You cannot see any details or modify it
                                here.<br>
                                Please contact your system administrator if you think this is wrong.
                            </v-alert>

                            <v-card class="elevation-12" v-if="!notAuthenticated && profile">
                                <v-toolbar dense flat color="indigo" dark>
                                    <v-toolbar-title>
                                        <v-icon left>far fa-address-card</v-icon>
                                        User Profile of {{ userData.name }} ({{ profile.firstname }} {{ profile.lastname }})
                                    </v-toolbar-title>
                                </v-toolbar>

                                <v-card-text>
                                    <div class="overline mb-4 mt-2">Profile Informations</div>

                                    <EditTextField
                                            :disabled="true"
                                            @submit="updateProfileInfo('username', $event)"
                                            icon="fas fa-user"
                                            v-model="userData.name"
                                            name="Username">
                                    </EditTextField>

                                    <EditTextField
                                            icon="fas fa-globe-europe"
                                            @submit="updateProfileInfo('displayName', $event)"
                                            v-model="profile.displayname"
                                            name="Display Name">
                                    </EditTextField>

                                    <EditTextField
                                            icon="fas fa-signature"
                                            @submit="updateProfileInfo('firstName', $event)"
                                            v-model="profile.firstname"
                                            name="First Name">
                                    </EditTextField>

                                    <EditTextField
                                            icon="fas fa-signature"
                                            @submit="updateProfileInfo('lastName', $event)"
                                            v-model="profile.lastname"
                                            name="Last Name">
                                    </EditTextField>

                                    <EditTextField
                                            @submit="updateProfileInfo('email', $event)"
                                            icon="far fa-envelope-open"
                                            v-model="profile.email"
                                            name="E-Mail Address">
                                    </EditTextField>

                                    <PasswordChangeInput
                                            @submit="updateProfileInfo('password', $event)"></PasswordChangeInput>

                                    <v-divider></v-divider>
                                    <div class="overline mb-4 mt-2">You'r user roles</div>
                                    <v-list>
                                        <template v-for="(item, index) in profile.roles">
                                            <v-list-item
                                                    v-if="item"
                                                    :key="item" @click="">
                                                <v-list-item-action>
                                                    <v-icon>fas fa-key</v-icon>
                                                </v-list-item-action>

                                                <v-list-item-content>
                                                    <v-list-item-title>{{ item }}</v-list-item-title>
                                                </v-list-item-content>
                                            </v-list-item>

                                            <v-divider
                                                    v-else-if="item.divider"
                                                    :key="index"
                                                    :inset="true"
                                            ></v-divider>
                                        </template>
                                    </v-list>

                                    <v-divider></v-divider>
                                    <div class="overline mb-4 mt-2">2FA Settings</div>

                                    <v-alert v-if="profile.totpEnabled" type="success" border="left">
                                        Well done! This Account is secured by 2FA.
                                    </v-alert>
                                    <v-alert v-if="!profile.totpEnabled" type="warning" border="left" outlined>
                                        This Account is not secured with 2FA! You should enable it to get better
                                        security.
                                    </v-alert>

                                    <v-btn color="success" outlined @click="createOtp" :loading="processingOtp"
                                           v-if="!profile.totpEnabled">
                                        Activate 2.FA
                                    </v-btn>

                                    <v-btn color="warning" outlined @click="disableOtp" v-if="profile.totpEnabled">
                                        Disable 2.FA
                                    </v-btn>
                                </v-card-text>
                            </v-card>
                        </v-flex>
                    </v-row>
                </v-container>
            </v-content>
        </v-parallax>

        <v-overlay v-model="processingOtp" persistent>
            <v-sheet width="300px" class="text-center">
                <RemoteImage :img="getQrCodeImage" class="pt-5 mb-4"></RemoteImage>
                <v-text-field v-model="otpCode" :error="otpError"
                              outlined placeholder="Code to Approve"
                              :error-messages="otpError ? 'Code is not valid' : null" class="pl-3 pr-3"
                              @keydown.enter="verifyOtp" append-icon="fas fa-times"
                              @click:append="abortOtp()"></v-text-field>
            </v-sheet>
        </v-overlay>

        <Footer/>
    </v-app>
</template>

<script>
    import axios from "axios";
    import Toolbar from "../components/Toolbar";
    import Footer from "../components/Footer";
    import EditTextField from "../components/EditTextField";
    import PasswordChangeInput from "../components/PasswordChangeInput";
    import RemoteImage from "../components/RemoteImage";
    import swal from 'sweetalert2';

    export default {
        components: {PasswordChangeInput, EditTextField, Toolbar, Footer, RemoteImage},
        data      : () => ({
            notAuthenticated: true,
            userData        : {},
            profile         : null,
            passwordChange  : null,
            processingOtp   : false,
            otpCode         : '',
            otpError        : false,
            errorInfo       : '',
        }),
        computed  : {
            getQrCodeImage() {
                return this.processingOtp ? '/api/profile/otp/qrcode' : null;
            }
        },
        methods   : {
            check() {
                axios.get('/api/me').then(res => {
                    this.userData         = res.data.data;
                    this.notAuthenticated = false;

                    this.getProfile();
                }).catch(() => {
                    this.$router.push('/login?service=/');
                    this.notAuthenticated = true;
                })
            },
            signOut() {
                axios.get('/api/deauth').then(() => {
                    this.notAuthenticated = true;
                    this.userData         = {};
                    this.profile          = null;
                    this.$router.push('/login?service=/');
                })
            },
            getProfile() {
                axios.get('/api/profile').then((res) => {
                    this.profile = res.data;
                }).catch((err) => {
                    if (err.response.status === 404) {
                        this.profile = false;
                    }
                })
            },
            updateProfileInfo(key, value) {
                axios.patch('/api/profile', {
                    field: key,
                    value: value,
                }).then(() => {
                    swal.fire('Yay!', 'Profile was updated!', 'success');
                }).catch((err) => {
                    this.errorInfo = err.toString();
                })
            },
            createOtp() {
                axios.get('/api/profile/otp').then(() => {
                    this.processingOtp = true;
                });
            },
            verifyOtp() {
                this.otpError = false;

                axios.post('/api/profile/otp', {
                    totp: this.otpCode,
                }).then(() => {
                    this.processingOtp = false;
                    this.getProfile();
                }).catch(() => {
                    this.otpError = true;
                });
            },
            disableOtp() {
                axios.delete('/api/profile/otp').then(() => {
                    this.getProfile();
                })
            },
            abortOtp() {
                this.processingOtp = false;
            }
        },
        mounted() {
            this.check();
        }
    }
</script>