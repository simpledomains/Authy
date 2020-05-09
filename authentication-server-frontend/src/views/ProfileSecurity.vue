<!--suppress JSUnresolvedVariable -->
<template>
    <v-auth-app location="Profile - Security">
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
                                    :loading="certificatesFetching"
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
                                    <v-btn x-small color="success" v-if="item.revokedAt == null" dark>NOT REVOKED YET
                                    </v-btn>
                                    <v-btn x-small color="error" v-else dark :title="timeSince(item.revokedAt)">{{
                                        formatDate(item.revokedAt) }}
                                    </v-btn>
                                </template>
                                <template v-slot:item.issuedAt="{ item }">
                                    <v-btn x-small color="primary" dark :title="timeSince(item.issuedAt)">{{
                                        formatDate(item.issuedAt) }}
                                    </v-btn>
                                </template>
                                <template v-slot:item.lastAccess="{ item }">
                                    <v-btn x-small color="primary" dark v-if="item.lastAccess"
                                           :title="formatDate(item.lastAccess)">
                                        {{ timeSince(item.lastAccess) }}
                                    </v-btn>
                                    <v-btn x-small color="gray" dark v-else>
                                        NEVER
                                    </v-btn>
                                </template>
                            </v-data-table>

                            <v-card-actions>
                                <v-btn color="success" :loading="certificatesFetching" dark small
                                       @click="fetchCertificates">
                                    <v-icon left>mdi-table-refresh</v-icon>
                                    Refresh
                                </v-btn>
                            </v-card-actions>

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
    import moment from 'moment'

    export default {
        components: {VAuthApp},
        data: () => ({
            certificates: [],

            certificateTableHeaders: [
                {text: 'Name', value: 'name'},
                {text: 'Serial', value: 'serial'},
                {text: 'Issued at', value: 'issuedAt'},
                {text: 'Revoked at', value: 'revokedAt'},
                {text: 'Last access', value: 'lastAccess'},
                {text: 'Actions', value: 'actions'},
            ],

            certificatesFetching: false,
        }),
        methods: {
            revokeCertificate(serial) {
                axios.delete('/api/session/me/certificate/' + serial).then(() => {
                    this.fetchCertificates();
                });
            },
            fetchCertificates() {
                this.certificatesFetching = true;
                axios.get('/api/session/me/certificates').then(r => {
                    this.certificates = r.data;
                }).finally(() => {
                    this.certificatesFetching = false;
                });
            },
            timeSince(date) {
                return moment(date, "YYYY-MM-DD[T]hh:mm:ss").fromNow();
            },
            formatDate(date) {
                return moment(date, "YYYY-MM-DD[T]hh:mm:ss").format('LLL');
            }
        },
        mounted() {
            this.fetchCertificates();
        }
    }
</script>