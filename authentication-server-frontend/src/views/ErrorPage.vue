<template>
    <v-auth-app location="Error">
        <v-content>
            <v-container>
                <v-row>
                    <v-col>
                        <v-alert type="error" color="error">
                            <strong>{{ getMessage($route.query.code) }}</strong>
                        </v-alert>
                    </v-col>
                </v-row>
            </v-container>
        </v-content>
    </v-auth-app>
</template>
<script>
    import VAuthApp from "../components/VAuthApp";

    export default {
        components: {VAuthApp},
        computed: {},
        data: () => ({
            errorMessages: {
                INVALID_SERVICE: 'The Service you\'r trying to authenticate against is not allowed to be authenticated by this CAS Server.',
                INVALID_TICKET: 'You\'r Ticket is not valid. It may be expired or used already.',
                MISSING_SERVICE: 'Service was not supplied or could not be extracted from request.',
                DENIED: 'Access to this Service was denied.',
            }
        }),
        methods: {
            getMessage(code) {
                if (this.errorMessages[code]) return this.errorMessages[code];
                return code;
            }
        }
    }
</script>