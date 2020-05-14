<template>
    <v-auth-app location="Users - Create">
        <v-content>
            <v-container>
                <v-row>
                    <v-col>
                        <v-card>
                            <v-card-title>User creation</v-card-title>
                            <v-card-subtitle>You can create a new User here.</v-card-subtitle>
                            <v-card-text>
                                <v-row>
                                    <v-col cols="12" md="6">
                                        <v-text-field label="Username" type="text" v-model="username"
                                                      prepend-icon="mdi-form-textbox"/>
                                    </v-col>
                                    <v-col cols="12" md="6">
                                        <v-text-field label="Display Name" type="text" v-model="displayName"
                                                      prepend-icon="mdi-form-textbox"/>
                                    </v-col>
                                    <v-col cols="12" md="6">
                                        <v-text-field label="Password" type="password" v-model="password"
                                                      prepend-icon="mdi-key"/>
                                    </v-col>
                                    <v-col cols="12" md="6">
                                        <v-text-field label="E-Mail" type="email" v-model="email"
                                                      prepend-icon="mdi-at"/>
                                    </v-col>
                                    <v-col cols="12" md="6">
                                        <v-select
                                                prepend-icon="mdi-account-group-outline"
                                                v-model="userRoles"
                                                :items="roles"
                                                multiple
                                                item-text="name"
                                                label="Authorities"
                                                chips small-chips
                                                item-value="id">
                                        </v-select>
                                    </v-col>
                                    <v-col cols="12" md="6">
                                        <v-checkbox label="Authy Admin?" prepend-icon="mdi-shield-lock-outline"
                                                    v-model="admin"/>
                                    </v-col>
                                </v-row>
                            </v-card-text>
                            <v-card-actions>
                                <v-btn small color="success" dark @click="createUser">
                                    <v-icon left>mdi-content-save</v-icon>
                                    Save
                                </v-btn>
                                <v-btn small color="orange darken-2" dark @click="abortCreation">
                                    <v-icon left>mdi-close-thick</v-icon>
                                    Abort
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
        data: () => ({
            roles: [],

            username: '',
            password: '',
            displayName: '',
            email: '',
            userRoles: [],
            admin: false,
        }),
        methods: {
            fetchRoles() {
                axios.get('/api/authorities').then(r => {
                    this.roles = r.data;
                })
            },
            resetForm() {
                this.username = '';
                this.password = '';
                this.displayName = '';
                this.email = '';
                this.userRoles = [];
                this.roles = [];
                this.admin = false;
            },
            abortCreation() {
                this.resetForm();
                this.$router.push('/admin/users');
            },
            createUser() {
                let roles = [];

                this.userRoles.forEach(value => roles.push({id: value}));

                axios.post('/api/identities', {
                    username: this.username,
                    password: this.password,
                    email: this.email,
                    displayName: this.displayName,
                    authorities: roles,
                    admin: this.admin,
                }).then(() => {
                    this.resetForm();
                    this.$router.push('/admin/users');
                }).catch(() => {
                    sw.fire({
                        type: 'error',
                        title: 'Failed to create user, check server log!'
                    })
                })
            }
        },
        mounted() {
            this.fetchRoles();
        }
    }
</script>