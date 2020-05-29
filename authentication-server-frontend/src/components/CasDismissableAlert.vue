<template>
    <v-alert dismissible :dark="dark" v-model="visible" :color="color" :type="type" :elevation="elevation">
        <slot/>
    </v-alert>
</template>

<script>
    export default {
        data: () => ({
            visible: true,
        }),
        props: {
            id: String,
            type: String,
            color: String,
            dark: {
                type: Boolean,
                default: false,
            },
            elevation: {
                type: String,
                default: "1"
            }
        },
        mounted() {
            this.visible = !(this.$localStore.state.dismissed[this.id])
        },
        watch: {
            visible() {
                this.$localStore.commit('dismiss', this.id);
            }
        }
    }
</script>