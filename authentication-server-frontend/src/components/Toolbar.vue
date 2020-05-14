<template>
    <div>
        <v-app-bar dense app color="primary" dark>
            <v-app-bar-nav-icon @click="drawer = !drawer" v-if="isAdmin"></v-app-bar-nav-icon>
            <v-icon left>fas fa-lock</v-icon>&nbsp; &nbsp;
            <v-toolbar-title>Authy - {{ location }}</v-toolbar-title>

            <div class="flex-grow-1"></div>
            <v-btn fab x-small color="pink" @click="signOut" elevation="1">
                <v-icon small>fas fa-sign-out-alt</v-icon>
            </v-btn>
        </v-app-bar>

        <v-navigation-drawer v-model="drawer" absolute temporary>
            <v-list-item>
                <v-list-item-content>
                    <v-list-item-title class="title">
                        Administration
                    </v-list-item-title>
                    <v-list-item-subtitle>
                        Users, Routes, ...
                    </v-list-item-subtitle>
                </v-list-item-content>
            </v-list-item>

            <v-divider></v-divider>

            <v-list>
                <v-list-item link to="/">
                    <v-list-item-icon>
                        <v-icon small>fas fa-desktop</v-icon>
                    </v-list-item-icon>
                    <v-list-item-content>
                        <v-list-item-title>
                            Frontpage
                        </v-list-item-title>
                    </v-list-item-content>
                </v-list-item>

                <v-list-item link to="/admin/users">
                    <v-list-item-icon>
                        <v-icon small>fas fa-users</v-icon>
                    </v-list-item-icon>
                    <v-list-item-content>
                        <v-list-item-title>
                            User Management
                        </v-list-item-title>
                    </v-list-item-content>
                </v-list-item>

                <v-list-item link to="/admin/authorities">
                    <v-list-item-icon>
                        <v-icon small>mdi-account-group-outline</v-icon>
                    </v-list-item-icon>
                    <v-list-item-content>
                        <v-list-item-title>
                            Authority Management
                        </v-list-item-title>
                    </v-list-item-content>
                </v-list-item>

                <v-list-item link to="/admin/services">
                    <v-list-item-icon>
                        <v-icon small>fas fa-globe</v-icon>
                    </v-list-item-icon>
                    <v-list-item-content>
                        <v-list-item-title>
                            Routes
                        </v-list-item-title>
                    </v-list-item-content>
                </v-list-item>
            </v-list>
        </v-navigation-drawer>
    </div>
</template>

<script>
    export default {
        data: () => ({
            drawer: false,
        }),
        props: {
            location: String,
        },
        methods: {
            openMenu() {
                if (this.isAdmin) {
                    this.drawer = true;
                }
            },
            closeMenu() {
                if (this.isAdmin) {
                    this.drawer = false;
                }
            },
            signOut() {
                this.$router.push("/logout");
            }
        },
        computed: {
            isAdmin() {
                return this.$store.state.admin
            }
        }
    }
</script>