<template>
    <basic-app location="Admin (Routes)">
        <v-content class="pl-3 pr-3">
            <v-row>
                <v-col>
                    <v-card elevation="1">
                        <v-toolbar dense flat color="primary" dark>
                            <v-toolbar-title>
                                <v-icon class="mb-1">mdi-earth</v-icon>
                                Routing & Services
                            </v-toolbar-title>
                        </v-toolbar>

                        <v-card-text>
                            <v-row>
                                <v-col class="col col-12 col-sm-12 col-md-6 col-lg-4 col-xl-3" v-for="item in services"
                                       :key="item.name">
                                    <service-visualization :item="item"
                                                           v-on:start="patchService($event, 'enabled', true)"
                                                           v-on:stop="patchService($event, 'enabled', false)"
                                                           v-on:delete="removeService($event)"/>
                                </v-col>
                            </v-row>
                        </v-card-text>

                        <v-card-actions>
                        </v-card-actions>
                    </v-card>
                </v-col>
            </v-row>
            <v-row>
                <v-col>
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
                                                  placeholder="URL to check (e.g.: https://google.com)"
                                                  dense></v-text-field>
                                </v-col>
                                <v-col cols="2">
                                    <v-btn @click="checkService" color="green" dark icon
                                           :loading="ui.isChecking">
                                        <v-icon>mdi-settings</v-icon>
                                    </v-btn>
                                </v-col>
                            </v-row>
                            <v-row v-if="checkedService != null">
                                <v-col cols="12">
                                    <service-visualization :item="checkedService"/>
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
    import ServiceVisualization from "../../components/ServiceVisualization";

    export default {
        components: {ServiceVisualization, BasicApp},
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
            },
            patchService(id, k, v) {
                let update = {};
                update[k] = v;

                axios.patch('/api/admin/route/' + id, update).catch(() => {
                    swal.fire('Oho!', 'Failed to patch Service.', 'error');
                }).finally(() => {
                    this.reloadServices();
                })
            },
            removeService(id) {
                this._askForApproval('Remove ' + id + '?', () => {
                    axios.delete('/api/admin/route/' + id)
                        .finally(() => {
                            this.reloadServices();
                        })
                });
            },
            getServiceById(id) {
                let service = null;

                this.services.forEach(value => {
                    if (value.id === id) service = value;
                });

                return service;
            },

            _askForApproval(text, callback) {
                swal.fire({
                    title: 'Are you sure?',
                    text: text,
                    icon: 'warning',
                    showCancelButton: true,
                    confirmButtonText: 'Approve',
                }).then(r => {
                    if (r.value) callback();
                })
            }
        },
        mounted() {
            this.reloadServices();
        }
    }
</script>