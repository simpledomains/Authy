<template>
    <v-footer color="primary" dark>
        <v-flex class="text-center my-1">
            <v-icon small left>far fa-copyright</v-icon>
            2019 - 2020 Made with
            <v-icon color="red">mdi-heart</v-icon>
            by SeaLife (build <span style="font-family: 'JetBrains Mono', monospace">{{ $version }}</span>)
        </v-flex>

        <v-switch style="position: absolute;bottom: 9px;" v-model="darkMode" @change="toggleDarkMode" color="white"
                  hide-details
                  prepend-icon="mdi-theme-light-dark"/>
    </v-footer>
</template>

<script>
    export default {
        data: () => ({
            darkMode: false
        }),
        methods: {
            toggleDarkMode() {
                this.$store.commit('toggleDarkMode');
                this.$vuetify.theme.dark = this.$store.state.darkMode;
            }
        },
        mounted() {
            this.$vuetify.theme.dark = this.$store.state.darkMode;
            this.darkMode = this.$store.state.darkMode;
        }
    }
</script>