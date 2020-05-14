<template>
    <v-auth-app :location="'Admin - Modify Service (' + $route.params.id + ')'">
        <v-content>
            <v-container>
                <v-row>
                    <v-col>
                        <v-card :loading="isFetchingServices">
                            <v-card-title>Modify Service ({{$route.params.id}})</v-card-title>
                            <v-card-subtitle>You can modify the selected Service here.</v-card-subtitle>
                            <v-card-text v-if="id !== null">
                                <v-row>
                                    <v-col cols="12" md="6">
                                        <v-text-field label="Service Name" type="text" v-model="name"
                                                      :rules="[validationMax25Chars, minimum3Chars]" validate-on-blur
                                                      prepend-icon="mdi-form-textbox"/>
                                    </v-col>
                                    <v-col cols="12" md="6">
                                        <v-select label="Service Mode" v-model="mode"
                                                  :disabled="requiredRoles.length > 0"
                                                  prepend-icon="mdi-shield-outline"
                                                  :items="['ADMIN', 'PUBLIC', 'ANONYMOUS', 'AUTHORIZED']"/>
                                    </v-col>
                                    <v-col cols="12" md="6">
                                        <v-combobox v-model="allowedUrls" multiple
                                                    prepend-icon="mdi-earth"
                                                    :items="allowedUrls"
                                                    :rules="[minimum1Item]"
                                                    :search-input.sync="urlSearch">
                                            <template v-slot:no-data>
                                                <v-list-item>
                                                    <v-list-item-content>
                                                        <v-list-item-title>
                                                            No results matching "<strong>{{ urlSearch }}</strong>".
                                                            Press
                                                            <kbd>enter</kbd> to create a new one
                                                        </v-list-item-title>
                                                    </v-list-item-content>
                                                </v-list-item>
                                            </template>
                                        </v-combobox>
                                    </v-col>
                                    <v-col cols="12" md="6">
                                        <v-select label="Required Roles (one of the following roles)"
                                                  v-model="requiredRoles"
                                                  prepend-icon="mdi-folder-key-network-outline" multiple
                                                  hint="This requires 'Service Mode' to be 'AUTHORIZED'"
                                                  :items="authorities" item-value="name" item-text="name"/>
                                    </v-col>
                                    <v-col cols="12" md="6">
                                        <v-checkbox label="Enabled?" prepend-icon="mdi-check-circle-outline"
                                                    v-model="enabled" :disabled="id===1"/>
                                    </v-col>
                                </v-row>
                            </v-card-text>
                            <v-card-actions>
                                <v-btn small color="success" dark @click="patchService" v-if="id !== null">
                                    <v-icon left>mdi-content-save</v-icon>
                                    Save
                                </v-btn>
                                <v-btn small color="orange darken-2" dark @click="abortModification">
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

    export default {
        components: {VAuthApp},
        data: () => ({
            id: null,
            name: '',
            mode: 'PUBLIC',
            enabled: true,
            requiredRoles: [],
            allowedUrls: [],

            urlSearch: '',

            authorities: [],

            isFetchingServices: true,
        }),
        methods: {
            validationMax25Chars(input) {
                if (input.length > 25) return 'Must not be longer than 25 characters.';
                return true;
            },
            minimum3Chars(input) {
                if (input.length < 3) return 'Input must be at least 3 characters long.';
                return true;
            },
            minimum1Item(input) {
                if (input.length === 0) return 'One item is required';
                return true;
            },
            fetchAuthorities() {
                axios.get('/api/authorities').then(r => this.authorities = r.data);
            },
            fetchService() {
                this.isFetchingServices = true;

                axios.get('/api/service/' + this.$route.params.id).then(r => {
                    this.mode = r.data.mode;
                    this.id = r.data.id;
                    this.requiredRoles = r.data.requiredRoles;
                    this.allowedUrls = r.data.allowedUrls;
                    this.enabled = r.data.enabled;
                    this.name = r.data.name;
                }).finally(() => {
                    this.isFetchingServices = false;
                })
            },
            patchService() {
                axios.patch('/api/service/' + this.id, {
                    data: {
                        name: this.name,
                        allowedUrls: this.allowedUrls,
                        mode: this.mode,
                        requiredRoles: this.requiredRoles,
                        enabled: this.enabled,
                    }
                }).then(() => {
                    this.$router.push('/admin/services');
                })
            },
            abortModification() {
                this.$router.push('/admin/services');
            }
        },
        mounted() {
            this.fetchService();
            this.fetchAuthorities();
        },
        watch: {
            requiredRoles: function (newValue) {
                if (newValue.length > 0)
                    this.mode = 'AUTHORIZED';
                else
                    this.mode = 'PUBLIC';
            }
        }
    }
</script>