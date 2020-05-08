<template>
    <v-auth-app location="My Profile">
        <v-content>
            <v-container>
                <v-alert color="red" dark type="error">
                    You'r signed in as {{ username }}. :)
                </v-alert>
            </v-container>
        </v-content>
    </v-auth-app>
</template>

<script>
    import VAuthApp from "../components/VAuthApp";
    export default {
        components: {VAuthApp},
        computed: {
            username() {
                return this.$store.state.username
            }
        }
    }
</script>