<template>
    <v-auth-app location="Admin - Services">
        <v-content>
            <v-container>
                <v-row>
                    <v-col>
                        <v-card elevation="2" outlined>
                            <v-card-title>Service Administration</v-card-title>
                            <v-card-subtitle>
                                You can setup the services this CAS instance is allowed to authorize.
                            </v-card-subtitle>
                            <v-data-table :items="services" :headers="tableServiceHeaders">
                                <template v-slot:item.name="{item}">
                                    <v-icon small color="green" v-if="item.enabled">mdi-check-circle-outline</v-icon>
                                    <v-icon small color="red" v-else>mdi-close-circle-outline</v-icon>
                                    {{item.name}}
                                </template>
                                <template v-slot:item.actions="{ item }">
                                    <v-btn icon small color="orange" :to="'/admin/service/' + item.id">
                                        <v-icon>mdi-pencil</v-icon>
                                    </v-btn>
                                    <v-btn icon small color="error" @click="deleteService(item)"
                                           :disabled="item.id===1">
                                        <v-icon>mdi-delete-empty-outline</v-icon>
                                    </v-btn>
                                </template>
                                <template v-slot:item.mode="{item}">
                                    <v-chip outlined small color="gray" v-if="item.mode === 'ANONYMOUS'">
                                        {{ item.mode }}
                                    </v-chip>
                                    <v-chip outlined small color="success" v-if="item.mode === 'PUBLIC'">
                                        {{ item.mode }}
                                    </v-chip>
                                    <v-chip outlined small color="error" v-if="item.mode === 'ADMIN'">
                                        {{ item.mode }}
                                    </v-chip>
                                    <v-chip outlined small color="info" v-if="item.mode === 'AUTHORIZED'">
                                        {{ item.mode }}
                                    </v-chip>
                                </template>
                            </v-data-table>

                            <v-card-actions>
                                <v-btn to="/admin/services/create" small color="success" dark elevation="4">
                                    <v-icon left>mdi-earth-plus</v-icon>
                                    Create Service
                                </v-btn>
                                <v-btn @click="fetchServices" small color="indigo" dark elevation="4">
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
        data: () => ({
            services: [],

            isFetchingServices: false,

            tableServiceHeaders: [
                {text: 'ID', value: 'id'},
                {text: 'Service Name', value: 'name'},
                {text: 'Mode', value: 'mode'},
                {text: 'URLs', value: 'allowedUrls'},
                {text: 'Required Roles', value: 'requiredRoles'},
                {text: 'Actions', value: 'actions'},
            ]
        }),
        methods: {
            fetchServices() {
                this.isFetchingServices = true;

                axios.get('/api/services').then(r => {
                    this.services = r.data;
                }).finally(() => {
                    this.isFetchingServices = false;
                })
            },
            deleteService(item) {
                sw.fire({
                    type: 'error',
                    title: 'Are you sure?',
                    html: 'Deleting the Service ' + item.name + ' cannot be undone!',
                    showCancelButton: true,
                }).then(r => {
                    if (r.value) {
                        axios.delete('/api/service/' + item.id).then(() => {
                            this.fetchServices();
                            sw.fire({type: 'success', title: 'Service deleted!'})
                        }).catch(() => {
                            sw.fire({
                                type: 'error',
                                title: 'Error',
                                text: 'Deleting the Service failed for some reason!\nIf this error persist, contact a system admin.'
                            })
                        })
                    }
                })
            }
        },
        mounted() {
            this.fetchServices();
        }
    }
</script>