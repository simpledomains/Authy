<template>
    <v-app>
        <v-content>
            <v-container>
                <v-alert color="red" dark type="error">
                    You'r signed in as {{ username }}. :)
                </v-alert>
            </v-container>
        </v-content>
    </v-app>
</template>

<script>
    export default {
        computed: {
            username() {
                return this.$store.state.username
            }
        }
    }
</script>