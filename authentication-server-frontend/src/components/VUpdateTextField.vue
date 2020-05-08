<template>
    <v-text-field :type="type" v-model="content" :label="label" :prepend-icon="icon"
                  @keydown.enter="update"></v-text-field>
</template>

<script>
    import axios from 'axios';
    import sw from 'sweetalert2';

    export default {
        props: ["type", "label", "initialValue", "icon", "objectKey"],
        data: () => ({
            content: ''
        }),
        methods: {
            update() {
                let model = {}
                model[this.objectKey] = this.content;

                axios.patch('/api/session/me', model).catch(() => {
                    sw.fire({
                        type: 'error',
                        title: 'Failed to update field ' + this.key,
                        html: 'If this issue persist, contact a admin about this.'
                    });
                })
            }
        },
        mounted() {
            this.content = this.initialValue;
        }
    }
</script>