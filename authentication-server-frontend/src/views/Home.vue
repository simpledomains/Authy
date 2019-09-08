<template>
    <v-app v-touch="{right: () => $refs.toolbar.openMenu(), left: () => $refs.toolbar.closeMenu()}">
        <Toolbar ref="toolbar" location="Home" :is-admin="user.admin" :logout-button="!!user.username"
                 @sign-out="signOut"/>

        <v-parallax height="100%"
                    src="https://images.unsplash.com/photo-1498050108023-c5249f4df085?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1952&q=80"
                    class="fill-height" v-if="!!user.username">
            <v-content>
                <v-container pa-5>
                    <v-row>
                        <v-flex xs12 sm12 lg8 offset-lg2>
                            <v-card>
                                <v-toolbar dark dense color="primary" flat>
                                    <v-toolbar-title>
                                        <v-icon left small>fas fa-user</v-icon>
                                        My Profile
                                    </v-toolbar-title>
                                </v-toolbar>

                                <v-card-text>
                                    Welcome back <b>{{ user.username }}</b>.
                                    <br><br>
                                    This page will be implemented later.
                                </v-card-text>
                            </v-card>
                        </v-flex>
                    </v-row>
                </v-container>
            </v-content>
        </v-parallax>

        <v-overlay v-model="processingOtp" persistent>
            <v-sheet width="300px" class="text-center">
                <!--<RemoteImage :img="getQrCodeImage" class="pt-5 mb-4"></RemoteImage>
                <v-text-field v-model="otpCode" :error="otpError"
                              outlined placeholder="Code to Approve"
                              :error-messages="otpError ? 'Code is not valid' : null" class="pl-3 pr-3"
                              @keydown.enter="verifyOtp" append-icon="fas fa-times"
                              @click:append="abortOtp()"></v-text-field>-->
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
        data: () => ({
            user: {},

            otpError: false,
            processingOtp: false,
        }),
        methods: {
            check() {
                axios.get('/api/me').then(res => {
                    this.user = res.data;
                }).catch(() => {
                    this.$router.push('/login?service=/');
                })
            },
            signOut() {
                axios.get('/api/logout').then(() => {
                    this.$router.push('/login?service=/');
                })
            },
            // OTP Functions
            otp: {
                init() {
                    axios.get('/api/profile/otp/start').then(() => {
                        this.processingOtp = true;
                    });
                },
                verify() {
                    this.otpError = false;

                    axios.post('/api/profile/otp', {
                        totp: this.otpCode,
                    }).then(() => {
                        this.processingOtp = false;
                        this.check();
                    }).catch(() => {
                        this.otpError = true;
                    });
                },
                disable() {
                    axios.delete('/api/profile/otp').then(() => {
                        this.check();
                    })
                },
                abort() {
                    this.processingOtp = false;
                }
            },
        },
        mounted() {
            this.check();
        }
    }
</script>