<template>
    <v-card light color="grey lighten-3" elevation="3"
            class="mb-1 mt-1" v-if="item">
        <v-card-title>
            <v-icon v-if="item.mode === 'PUBLIC'" left>mdi-earth</v-icon>
            <v-icon v-if="item.mode === 'AUTHORIZED'" left>mdi-cloud-lock-outline</v-icon>
            <v-icon v-if="item.mode === 'ANONYMOUS'" left>mdi-incognito</v-icon>
            <v-icon v-if="item.mode === 'ADMIN'" left>mdi-lock-alert</v-icon>
            {{ item.name }}
        </v-card-title>

        <v-card-text>
            <v-card elevation="1" outlined color="primary lighten-3" class="pa-1" v-if="item.allowedUrls">
                <code v-for="(i, x) in item.allowedUrls" :key="x" class="mr-1">{{i}}</code>
            </v-card>

            <v-card v-if="item.mode === 'AUTHORIZED' && item.requiredRoles" elevation="1" outlined
                    color="green lighten-3" class="pa-1 mt-1">
                <code v-for="(i, x) in item.requiredRoles" :key="x" class="mr-1">{{i}}</code>
            </v-card>
        </v-card-text>

        <v-card-actions>
            <v-btn small color="grey" icon :disabled="item.id === 1" @click="$emit('edit', item.id)">
                <v-icon>mdi-pencil</v-icon>
            </v-btn>
            <v-btn small color="red" icon :disabled="item.id === 1" @click="$emit('delete', item.id)">
                <v-icon>mdi-delete</v-icon>
            </v-btn>
            <v-btn small color="red" icon :disabled="item.id === 1" v-if="item.enabled" @click="$emit('stop', item.id)">
                <v-icon>mdi-pause</v-icon>
            </v-btn>
            <v-btn small color="green" icon :disabled="item.id === 1" v-if="!item.enabled" @click="$emit('start', item.id)">
                <v-icon>mdi-play</v-icon>
            </v-btn>
        </v-card-actions>
    </v-card>
</template>

<script>
    export default {
        props: ['item']
    }
</script>