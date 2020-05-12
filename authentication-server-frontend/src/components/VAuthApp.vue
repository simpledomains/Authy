<template>
    <v-app>
        <app-toolbar :location="location"/>

        <slot/>

        <app-footer/>
    </v-app>
</template>
<script>
    import AppToolbar from "./Toolbar";
    import AppFooter from './Footer';

    export default {
        components: {AppToolbar, AppFooter},
        props: ['location'],
        data: () => ({
            user: null,
        })
    }
</script>