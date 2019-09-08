<template>
    <v-card>
        <v-toolbar dense :color="color">
            <v-toolbar-title>
                {{ title }}
            </v-toolbar-title>
        </v-toolbar>

        <v-card-text>
            <slot/>
        </v-card-text>
    </v-card>
</template>

<script>
    export default  {
        props: ['title', 'color']
    }
</script>