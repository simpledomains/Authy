<template>
    <v-auth-app :location="'Users - Modify (' + this.$route.params.id + ')'">
        <v-content>
            <v-container>
                <v-card :loading="loadingUser">
                    <v-card-title>User modification</v-card-title>
                    <v-card-subtitle>You can modify the user here!</v-card-subtitle>
                    <v-card-text>
                        <v-row>
                            <v-col cols="6">
                                <v-text-field label="Username" type="text" v-model="username"
                                              prepend-icon="mdi-form-textbox"/>
                            </v-col>
                            <v-col cols="6">
                                <v-text-field label="Display Name" type="text" v-model="displayName"
                                              prepend-icon="mdi-form-textbox"/>
                            </v-col>
                            <v-col cols="6">
                                <v-text-field label="Password" type="password" v-model="password"
                                              prepend-icon="mdi-key"/>
                            </v-col>
                            <v-col cols="6">
                                <v-text-field label="E-Mail" type="email" v-model="email" prepend-icon="mdi-at"/>
                            </v-col>
                            <v-col cols="6">
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
                            <v-col cols="6">
                                <v-checkbox label="Authy Admin?" prepend-icon="mdi-shield-lock-outline"
                                            v-model="admin"/>
                            </v-col>
                        </v-row>
                    </v-card-text>
                    <v-card-actions>
                        <v-btn small color="success" dark @click="patchUser">
                            <v-icon left>mdi-content-save</v-icon>
                            Save
                        </v-btn>
                        <v-btn small color="orange darken-2" dark @click="abortCreation">
                            <v-icon left>mdi-close-thick</v-icon>
                            Abort
                        </v-btn>
                    </v-card-actions>
                </v-card>
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

            id: null,
            username: '',
            password: '',
            displayName: '',
            email: '',
            userRoles: [],
            admin: false,

            loadingUser: true,
        }),
        methods: {
            fetchRoles() {
                axios.get('/api/authorities').then(r => {
                    this.roles = r.data;
                })
            },
            fetchUser() {
                axios.get('/api/user/' + this.$route.params.id).then(r => {
                    this.id = r.data.id;
                    this.username = r.data.username;
                    this.displayName = r.data.displayName;
                    this.email = r.data.email;
                    this.admin = r.data.admin;
                    this.userRoles = [];

                    r.data.authorities.forEach(value => this.userRoles.push(value.id));

                    this.loadingUser = false;
                }).catch(() => {
                    sw.fire({
                        type: 'error',
                        title: 'Failed to fetch the user, going back.'
                    });
                    this.$router.push('/admin/users');
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
            patchUser() {
                let roles = [];
                this.userRoles.forEach(value => roles.push({id: value}));

                let model = {};
                model.id = this.id;
                model.username = this.username;
                model.admin = this.admin;
                if (this.password !== "") model.password = this.password;
                model.email = this.email;
                model.displayName = this.displayName;
                model.authorities = roles;


                axios.patch('/api/user/' + this.id, {data: model}).then(() => {
                    this.resetForm();
                    this.$router.push('/admin/users');
                }).catch(() => {
                    sw.fire({
                        type: 'error',
                        title: 'Failed to update user, check server log!'
                    })
                })
            }
        },
        mounted() {
            this.fetchRoles();
            this.fetchUser();
        }
    }
</script>