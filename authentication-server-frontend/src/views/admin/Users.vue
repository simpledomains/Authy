<template>
    <v-app v-touch="{right: () => $refs.toolbar.openMenu(), left: () => $refs.toolbar.closeMenu()}">
        <Toolbar ref="toolbar" location="User Admin" :is-admin="userData && userData.admin" :logout-button="userData !== null"/>

        <v-parallax height="100%"
                    src="https://images.unsplash.com/photo-1498050108023-c5249f4df085?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1952&q=80"
                    class="fill-height">
            <!-- Content goes here -->

            <v-content>
                <v-container>
                    <v-layout>
                        <v-flex lg12>
                            <card-content title="User Administration" color="primary">
                                <crud-table url="/api/admin/users" :headers="[{text: 'Username', value: 'username'}, {text: 'E-Mail', value: 'email'}, {text: 'First name', value: 'firstname'}, {text: 'Last name', value: 'lastname'}, {text: 'Roles', value: 'roles'}]">
                                    <template v-slot:item.username="{ item }">
                                        <v-chip :color="red" dark>{{ item.username }}</v-chip>
                                    </template>
                                    <template v-slot:item.roles="{ item }">
                                        <kbd>{{ item.roles.length }} Role(s)</kbd>
                                    </template>
                                </crud-table>
                            </card-content>
                        </v-flex>
                    </v-layout>
                </v-container>
            </v-content>

        </v-parallax>

        <Footer/>
    </v-app>
</template>

<script>
    import Toolbar from "../../components/Toolbar";
    import Footer from "../../components/Footer";
    import axios from "axios";
    import CrudTable from "../../components/CrudTable";
    import CardContent from "../../components/CardContent";

    export default {
        components: {CardContent, CrudTable, Toolbar, Footer},
        data      : () => ({
            userData: null,
        }),
        methods   : {
            getCurrentUser() {
                axios.get('/api/me').then(res => {
                    this.userData = res.data.data;
                })
            }
        },
        mounted() {
            this.getCurrentUser();
        }
    }
</script>