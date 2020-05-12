<template>
    <v-auth-app location="Users">
        <v-content>
            <v-container>
                <v-row>
                    <v-col>

                        <v-card :loading="states.loadingUsers">
                            <v-data-table :headers="user_table.headers" :items="users" item-key="id" multi-sort>
                                <template v-slot:item.flags="{ item }">
                                    <v-icon :color="getColorState(item.admin)" left>mdi-shield-lock-outline</v-icon>
                                    <v-icon :color="getColorState(item.locked, 'error')" left>mdi-lock-alert</v-icon>
                                    <v-icon :color="getColorState(item.otpEnabled)" left>mdi-cellphone-key</v-icon>
                                </template>
                                <template v-slot:item.actions="{ item }">
                                    <v-btn :disabled="currentUserId === item.id" icon small
                                           :to="'/admin/user/' + item.id">
                                        <v-icon color="gray">mdi-lead-pencil</v-icon>
                                    </v-btn>
                                    <v-btn :disabled="currentUserId === item.id" icon small
                                           @click="deleteUser(item.id)">
                                        <v-icon color="gray">mdi-delete-empty-outline</v-icon>
                                    </v-btn>
                                    <v-btn v-if="item.otpEnabled" icon small color="red"
                                           @click="removeOtp(item.id)">
                                        <v-icon color="gray">mdi-key-remove</v-icon>
                                    </v-btn>
                                </template>
                            </v-data-table>
                            <v-card-actions>
                                <v-btn to="/admin/users/create" small color="success" dark elevation="4">
                                    <v-icon left>mdi-account-plus-outline</v-icon>
                                    Create User
                                </v-btn>
                                <v-btn @click="fetchUsers" small color="indigo" dark elevation="4">
                                    <v-icon left>mdi-table-refresh</v-icon>
                                    Refresh
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
    import sw from 'sweetalert2';

    export default {
        components: {VAuthApp},
        computed: {
            currentUserId() {
                return this.$store.state.uid;
            }
        },
        data: () => ({
            users: [],
            user_table: {
                headers: [
                    {text: 'User ID', value: 'id'},
                    {text: 'Username', value: 'username'},
                    {text: 'Display Name', value: 'displayName'},
                    {text: 'Flags', value: 'flags'},
                    {text: 'Actions', value: 'actions'},
                ]
            },
            states: {
                loadingUsers: false,
            }
        }),
        methods: {
            fetchUsers() {
                this.states.loadingUsers = true;

                axios.get("/api/users").then(r => {
                    this.users = r.data;
                }).catch(e => {
                    alert(e.response.data);
                }).finally(() => {
                    this.states.loadingUsers = false;
                })
            },
            getColorState(flag, activeColor) {
                if (flag) return activeColor ? activeColor : 'green';
                return 'gray';
            },
            removeOtp(userId) {
                sw.fire({
                    type: 'info',
                    title: 'Are you sure?',
                    showCancelButton: true,
                    showConfirmButton: true
                }).then(r => {
                    if (r.value) {
                        axios.patch('/api/user/' + userId, {
                            data: {
                                otpSecret: null
                            }
                        }).then(() => {
                            sw.fire('OTP removed.').then(() => this.fetchUsers());
                        }).catch(() => {
                            sw.fire({
                                type: 'error',
                                title: 'OTP was not removed! Check server log!'
                            })
                        })
                    }
                })
            },
            deleteUser(userId) {
                sw.fire({
                    title: 'Are you sure?',
                    showCancelButton: true,
                    showConfirmButton: true
                }).then(r => {
                    if (r.value) {
                        axios.delete('/api/user/' + userId).then(() => {
                            sw.fire('User removed.').then(() => this.fetchUsers());
                        }).catch(() => {
                            sw.fire({
                                type: 'error',
                                title: 'User was not removed! Check server log!'
                            })
                        })
                    }
                })
            }
        },
        mounted() {
            this.fetchUsers();
        }
    }
</script>