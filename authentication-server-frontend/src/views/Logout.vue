<template>
    <span>logout...</span>
</template>

<script>
    import axios from 'axios'

    export default {
        mounted() {
            axios.delete('/cas/logout').then(() => {
                this.$store.commit('removeToken');

                if (this.$route.query.redirectTo && this.$route.query.redirectTo.startsWith('http')) {
                    window.location.href = this.$route.query.redirectTo;
                } else {
                    this.$router.push({path: '/login', query: {service: '/'}});
                }
            });
        }
    }
</script>