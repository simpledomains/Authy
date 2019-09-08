<template>
    <v-app>
        <Toolbar location="Error" :is-admin="false"></Toolbar>

        <v-content>
            <v-container>
                <v-row>
                    <v-flex xs8 offset-xs2 pt-5>
                        <v-alert type="error" border="left" prominent colored-border v-if="$route.query.code === 'INVALID_SERVICE'">
                            The service (<a :href="$route.query.service">{{ $route.query.service }}</a>) is not allowed.<br>
                            If you dont like this, you should contact your system administrator.
                        </v-alert>
                        <v-alert type="warning" border="left" colored-border v-if="$route.query.code === 'MISSING_SERVICE'">
                            This Service is not allowed to authenticate through this Service.
                        </v-alert>
                        <v-alert type="error" border="left" prominent colored-border v-if="$route.query.code === 'USER_REVOKED'">
                            You'r account was blocked by a Administrator. You can no longer access this service.
                        </v-alert>
                        <v-alert type="warning" border="left" colored-border v-if="$route.query.code === 'DENIED'">
                            You'r not allowed to access this resource. Please contact your system administrator for getting permission.
                        </v-alert>
                    </v-flex>
                </v-row>
            </v-container>
        </v-content>

        <Footer />
    </v-app>
</template>

<script>
    import Footer from "../components/Footer";
    import Toolbar from "../components/Toolbar";

    export default {
        data   : () => ({}),
        components: {Toolbar, Footer},
        methods: {}
    }
</script>