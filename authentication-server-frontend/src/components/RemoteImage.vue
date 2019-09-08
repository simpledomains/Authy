<template>
    <img v-if="imageData" :src="imageData"/>
</template>

<script>
    import axios from "axios";

    export default {
        props  : {
            img: String
        },
        data   : () => ({
            imageData: '',
        }),
        methods: {
            reset() {
                this.imageData = null;
            },
            reload() {
                let requestOptions = {
                    method : 'GET',
                    headers: {'accept': 'dataurl'},
                    url    : '/api/profile/otp/qrcode'
                };

                axios(requestOptions).then(d => this.imageData = d.data);
            },
        },
        mounted() {
            this.reload();
        }
    }
</script>