<template>
    <v-app v-touch="{right: () => $refs.toolbar.openMenu(), left: () => $refs.toolbar.closeMenu()}">
        <toolbar ref="toolbar" :location="location" :is-admin="store.state.admin" logout-button/>

        <slot/>

        <app-footer/>
    </v-app>
</template>
<script>
    import Toolbar from "./Toolbar";
    import AppFooter from './Footer';
    import axios from "axios";

    export default {
        components: {Toolbar, AppFooter},
        props: ['location'],
        data: () => ({
            user: null,
        }),
        methods: {
            getCurrentUser(force) {
                this.user = JSON.parse(localStorage.getItem('AuthyUser'));

                if (!this.user || force) {
                    axios.get('/api/me').then(r => {
                        localStorage.setItem('AuthyUser', JSON.stringify(r.data));
                        this.user = r.data;
                    }).catch(() => {
                        this.$router.push('/login?service=/');
                    });
                }
            },
        },
        mounted() {
            this.getCurrentUser(true);
            this.$frontendConfiguration(this.$vuetify);
        }
    }
</script>