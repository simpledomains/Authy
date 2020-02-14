<template>
    <v-footer color="primary" dark>
        <v-flex class="text-center my-1">
            <v-icon small left>far fa-copyright</v-icon>
            2019 - Made with
            <v-icon small right left color="red">mdi-heart</v-icon>
            by SeaLife (<span v-if="($version).startsWith('v')">{{ $version }}</span><span v-else>build {{ $version }}</span>)
        </v-flex>
    </v-footer>
</template>

<script>
    export default {}
</script>