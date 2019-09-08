<template>
    <v-app v-touch="{right: () => $refs.toolbar.openMenu(), left: () => $refs.toolbar.closeMenu()}">
        <Toolbar ref="toolbar" location="Route Admin" :is-admin="userData.admin"/>

        <v-content>
            <v-container pa-5>
                <v-row v-if="!userData.admin">
                    <v-flex xs12 sm12 lg8 offset-lg2 pt-5>
                        <v-alert type="error" border="left" prominent outlined>
                            Access Denied. You need Admin Privileges to access this page!
                        </v-alert>
                    </v-flex>
                </v-row>
                <v-row v-if="!appStatus.databaseEnabled">
                    <v-flex xs12 sm12 lg8 offset-lg2 pt-5>
                        <v-alert type="info" border="left" colored-border>
                            You need to enable the Database Integration in order to use this.
                        </v-alert>
                    </v-flex>
                </v-row>
                <v-row v-if="userData.admin && appStatus.databaseEnabled">
                    <v-flex xs12 sm12 lg8 offset-lg2>
                        <v-card dense :loading="refreshData">
                            <v-toolbar color="warning" dark dense>
                                <v-toolbar-title>
                                    <v-icon small left>fas fa-globe</v-icon>
                                    Route Administration
                                </v-toolbar-title>

                                <div class="flex-grow-1"></div>
                                <v-btn right icon @click="refreshRoles" :loading="refreshData">
                                    <v-icon small>fas fa-redo</v-icon>
                                </v-btn>
                            </v-toolbar>

                            <v-simple-table fixed-header height="300px">
                                <thead>
                                <tr>
                                    <th class="text-left">Protocol</th>
                                    <th class="text-left">Host</th>
                                    <th class="text-left">Path</th>
                                    <th class=""></th>
                                </tr>
                                </thead>
                                <tbody>
                                <tr v-for="item in routes" :key="item.name">
                                    <td style="font-family: monospace, monospace">
                                        {{ item.protocol || '(empty)' }}
                                    </td>
                                    <td style="font-family: monospace, monospace">
                                        {{ item.host || '(empty)' }}
                                    </td>
                                    <td style="font-family: monospace, monospace">
                                        {{ item.path || '(empty)' }}
                                    </td>
                                    <td class="text-right">
                                        <v-btn icon @click="">
                                            <v-icon small>fas fa-trash-alt</v-icon>
                                        </v-btn>
                                        <v-btn icon @click="">
                                            <v-icon small>fas fa-pen</v-icon>
                                        </v-btn>
                                    </td>
                                </tr>
                                <tr v-if="routes.length === 0">
                                    <td colspan="4">
                                        No Routes found.
                                    </td>
                                </tr>
                                </tbody>
                            </v-simple-table>
                            <v-card-actions>
                                <v-btn color="success" rounded @click="expandCreate = !expandCreate">
                                    <v-icon small left>fas fa-plus</v-icon>
                                    Create Route
                                </v-btn>
                            </v-card-actions>
                        </v-card>
                    </v-flex>
                </v-row>
                <v-row v-if="userData.admin && appStatus.databaseEnabled">
                    <v-flex xs12 sm12 lg8 offset-lg2 pt-4>
                        <v-expand-transition>
                            <v-card v-show="expandCreate" dense :loading="refreshData">
                                <v-toolbar color="warning" dark dense>
                                    <v-toolbar-title>
                                        <v-icon small left>fas fa-cogs</v-icon>
                                        Create a new Route
                                    </v-toolbar-title>
                                </v-toolbar>
                            </v-card>
                        </v-expand-transition>
                    </v-flex>
                </v-row>
            </v-container>
        </v-content>

        <Footer/>
    </v-app>
</template>

<script>
    import Toolbar from "../../components/Toolbar";
    import Footer from "../../components/Footer";
    import axios from "axios";

    export default {
        components: {Toolbar, Footer},
        data      : () => ({
            userData        : {admin: true},
            refreshData     : {},
            routes          : [],
            urlToCheck: '',
            appStatus       : {
                databaseEnabled: true,
            },
            search          : '',
            menu            : false,
            expandCreate: false,
        }),
        methods   : {
            refreshRoles() {
                this.refreshData = true;

                axios.get('/api/admin/routes').then(res => {
                    this.refreshData = false;
                    this.routes       = res.data;
                }).catch(() => {
                    this.refreshData = false;
                });
            },
            check() {
                axios.get('/api/me').then(res => {
                    this.userData         = res.data.data;
                    this.notAuthenticated = false;
                }).catch((err) => {
                    this.userData         = {};
                    this.notAuthenticated = true;
                })
            },
            getAppStatus() {
                axios.get('/api/app-status').then(res => {
                    this.appStatus = res.data;
                });
            },
        },
        mounted() {
            this.getAppStatus();
            this.check();
            this.refreshRoles();
        }
    }
</script>