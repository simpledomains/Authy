<template>
    <span>logout...</span>
</template>

<script>
    import axios from 'axios'

    export default {
        mounted() {
            axios.delete('/cas/logout').then(() => {
                this.$store.commit('removeToken');
                this.$router.push({path: '/login', query: {service: '/'}});
            });
        }
    }
</script>