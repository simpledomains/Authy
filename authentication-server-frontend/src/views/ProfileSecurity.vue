<template>
    <v-auth-app>
        <v-content>
            <v-container>
                <v-row>
                    <v-col>
                        <v-card>
                            <v-card-title>My Profile - Security</v-card-title>
                            <v-card-subtitle>
                                Security related options like 2FA and mTLS is configured here.
                            </v-card-subtitle>
                            <v-divider/>

                            <v-card-title>mTLS Settings</v-card-title>
                            <v-data-table
                                    :items="certificates"
                                    :headers="certificateTableHeaders">
                                <template v-slot:item.name="{ item }">
                                    <i>{{ item.name }}</i>
                                </template>
                                <template v-slot:item.actions="{ item }">
                                    <v-btn icon color="error" v-if="item.revokedAt == null"
                                           @click="revokeCertificate(item.serial)">
                                        <v-icon>mdi-delete</v-icon>
                                    </v-btn>
                                </template>
                                <template v-slot:item.revokedAt="{ item }">
                                    <v-btn x-small color="success" v-if="item.revokedAt == null" dark>NOT REVOKED YET</v-btn>
                                    <v-btn x-small color="error" v-else dark>{{ item.revokedAt }}</v-btn>
                                </template>
                                <template v-slot:item.issuedAt="{ item }">
                                    <v-btn x-small color="primary" dark>{{ item.issuedAt }}</v-btn>
                                </template>
                            </v-data-table>

                            <v-divider/>
                            <v-card-title>2FA Settings</v-card-title>
                            <v-card-text>
                                <v-alert type="info" color="info" elevation="5">
                                    Not implemented yet.
                                </v-alert>
                            </v-card-text>

                            <v-card-actions>
                                <v-btn color="orange" to="/" small dark elevation="4">
                                    <v-icon left>mdi-arrow-left</v-icon>
                                    to Profile
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
    import axios from 'axios';

    export default {
        components: {VAuthApp},
        data: () => ({
            certificates: [],

            certificateTableHeaders: [
                {text: 'Name', value: 'name'},
                {text: 'Serial', value: 'serial'},
                {text: 'Issued at', value: 'issuedAt'},
                {text: 'Revoked at', value: 'revokedAt'},
                {text: 'Actions', value: 'actions'},
            ]
        }),
        methods: {
            revokeCertificate(serial) {
                axios.delete('/api/session/me/certificate/' + serial).then(() => {
                    this.fetchCertificates();
                });
            },
            fetchCertificates() {
                axios.get('/api/session/me/certificates').then(r => {
                    this.certificates = r.data;
                });
            }
        },
        mounted() {
            this.fetchCertificates();
        }
    }
</script>