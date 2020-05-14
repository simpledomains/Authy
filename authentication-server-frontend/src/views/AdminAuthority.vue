<template>
    <v-auth-app location="Admin - Authorities">
        <v-content>
            <v-container>
                <v-row>
                    <v-col cols="12" md="3">
                        <v-card elevation="2">
                            <v-card-title>Create Authority</v-card-title>
                            <v-card-text>
                                <v-text-field v-model="roleName" clearable @keydown.enter="createRole">
                                </v-text-field>
                            </v-card-text>
                        </v-card>
                    </v-col>
                    <v-col cols="12" md="9">
                        <v-card elevation="2">
                            <v-card-title>Authorities</v-card-title>
                            <v-data-table :items="roles" :headers="table_headers" :loading="isFetching">
                                <template v-slot:item.name="{ item }">
                                    <span style="font-family: 'JetBrains Mono','Droid Sans Mono',monospace;font-weight: bold;">
                                        {{ item.name }}
                                    </span>
                                </template>
                                <template v-slot:item.actions="{ item }">
                                    <v-btn icon small @click="deleteRole(item)">
                                        <v-icon color="gray">mdi-delete-empty-outline</v-icon>
                                    </v-btn>
                                </template>
                            </v-data-table>
                        </v-card>
                    </v-col>
                </v-row>
            </v-container>
        </v-content>
    </v-auth-app>
</template>
<script>
    import VAuthApp from "../components/VAuthApp";
    import axios from "axios";
    import sw from 'sweetalert2';

    export default {
        components: {VAuthApp},
        data: () => ({
            roles: [],

            isFetching: false,

            roleName: '',

            table_headers: [
                {text: 'Role ID', value: 'id'},
                {text: 'Role Name', value: 'name'},
                {text: 'Actions', value: 'actions'},
            ]
        }),
        methods: {
            fetchRoles() {
                this.isFetching = true;

                axios.get('/api/authorities').then(r => {
                    this.roles = r.data;
                }).finally(() => {
                    this.isFetching = false;
                });
            },
            deleteRole(item) {
                sw.fire({
                    type: 'warning',
                    title: 'Are you sure?',
                    text: 'Deleting the role ' + item.name + ' cannot be undone!',
                    showCancelButton: true
                }).then((r) => {
                    if (r.value) {
                        axios.delete('/api/authority/' + item.id).finally(() => {
                            this.fetchRoles();

                            sw.fire({type: 'success', title: 'Role was deleted!'})
                        })
                    }
                })
            },
            createRole() {
                axios.post('/api/authorities', {
                    name: this.roleName,
                }).finally(() => {
                    this.fetchRoles();
                    this.roleName = '';

                    sw.fire({type: 'success', title: 'Role was created!'})
                })
            }
        },
        mounted() {
            this.fetchRoles();
        }
    }
</script>