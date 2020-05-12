<template>
    <v-auth-app location="Error">
        <v-content>
            <v-container>
                <v-alert>
                    <strong>{{ $route.params.code }}</strong>
                </v-alert>
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
                INVALID_TICKET: 'You\'r Ticket is not valid. It may be expired or used already.'
            }
        })
    }
</script>