<template>
    <basic-app location="Admin (Routes)">
        <v-content class="pl-3 pr-3">
            <v-row>
                <v-col offset="3" cols="6">
                    <v-card>
                        <v-toolbar dense flat color="primary" dark>
                            <v-toolbar-title>
                                <v-icon class="mb-1">mdi-earth</v-icon>
                                Routing & Services
                            </v-toolbar-title>
                        </v-toolbar>
                        <v-simple-table fixed-header>
                            <template v-slot:default>
                                <thead>
                                <tr>
                                    <th style="width: 8px"></th>
                                    <th style="width: 10px"></th>
                                    <th class="text-left">Name</th>
                                    <th class="text-left">Service URL</th>
                                    <th class="text-left">Required Roles (any of)</th>
                                </tr>
                                </thead>
                                <tbody>
                                <tr v-for="item in services" :key="item.name">
                                    <td>
                                        <v-icon v-if="item.enabled" color="success">mdi-check-circle-outline</v-icon>
                                        <v-icon v-if="!item.enabled" color="red">mdi-close-circle-outline</v-icon>
                                    </td>
                                    <td>
                                        <v-icon v-if="item.mode === 'PUBLIC'">mdi-earth</v-icon>
                                        <v-icon v-if="item.mode === 'AUTHORIZED'">mdi-cloud-lock-outline</v-icon>
                                        <v-icon v-if="item.mode === 'ANONYMOUS'">mdi-incognito</v-icon>
                                        <v-icon v-if="item.mode === 'ADMIN'">mdi-lock-alert</v-icon>
                                    </td>
                                    <td>
                                        <v-chip color="pink" small outlined>{{ item.name }}</v-chip>
                                    </td>
                                    <td>
                                        <v-chip class="mr-2" color="black" small outlined
                                                v-for="(i, x) in item.allowedUrls" :key="x">
                                            {{ i }}
                                        </v-chip>
                                    </td>
                                    <td>
                                        <v-chip class="mr-2" color="primary" small outlined
                                                v-for="(i, x) in item.requiredRoles" :key="x">
                                            {{ i }}
                                        </v-chip>
                                    </td>
                                </tr>
                                </tbody>
                            </template>
                        </v-simple-table>
                    </v-card>
                </v-col>
            </v-row>
            <v-row>
                <v-col cols="6" offset="3">
                    <v-card>
                        <v-toolbar dense color="success" dark elevation="0">
                            <v-toolbar-title>
                                <v-icon class="mb-1">mdi-test-tube</v-icon>
                                Test your Routes
                            </v-toolbar-title>
                        </v-toolbar>
                        <v-card-text>
                            <v-row>
                                <v-col cols="10">
                                    <v-text-field @keydown.enter="checkService" v-model="serviceUrl"
                                                  :loading="ui.isChecking"
                                                  placeholder="URL to check (e.g.: https://google.com)"></v-text-field>
                                </v-col>
                                <v-col cols="2">
                                    <v-btn @click="checkService" color="green" dark large block
                                           :loading="ui.isChecking">
                                        <v-icon class="mr-3">mdi-settings</v-icon>
                                        Check
                                    </v-btn>
                                </v-col>
                            </v-row>
                            <v-row v-if="checkedService != null">
                                <v-col cols="12">
                                    You'r requested Domain will resolve to the Service called
                                    <v-chip color="pink" small outlined>{{ checkedService.name }}</v-chip>.
                                    <div v-if="checkedService.mode === 'AUTHORIZED'">
                                        <br>This Service requires at least one of the following roles:<br><br>
                                        <v-chip class="mr-2" color="primary" small outlined
                                                v-for="(i, x) in checkedService.requiredRoles" :key="x">
                                            {{ i }}
                                        </v-chip>
                                    </div>
                                </v-col>
                            </v-row>
                        </v-card-text>
                    </v-card>
                </v-col>
            </v-row>
        </v-content>
    </basic-app>
</template>

<script>
    import BasicApp from "../../components/BasicApp";
    import axios from 'axios';
    import swal from 'sweetalert2';

    export default {
        components: {BasicApp},
        data: () => ({
            services: [],
            serviceUrl: '',

            checkedService: null,

            ui: {
                isChecking: false,
            }
        }),
        methods: {
            reloadServices() {
                axios.get('/api/admin/routes').then(r => r.data).then(data => {
                    this.services = data;
                });
            },
            checkService() {
                this.ui.isChecking = true;

                axios.get('/api/admin/route/find?service=' + this.serviceUrl).then(r => {
                    this.checkedService = r.data;
                }).catch(() => {
                    this.checkedService = null;
                    swal.fire('Oho!', 'Domain ' + this.serviceUrl + ' does not resolve to any active service!', 'error');
                }).finally(() => {
                    this.ui.isChecking = false;
                })
            }
        },
        mounted() {
            this.reloadServices();
        }
    }
</script>