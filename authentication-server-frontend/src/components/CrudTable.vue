<template>
    <div>
        <v-data-table v-if="!httpError" :headers="headers" :items="items" :single-select="true">
            <template v-slot:item="{item, headers}">
                <tr>
                    <td v-for="(h, idx) in headers" :key="idx">
                        <slot :name="'item.' + h.value" v-bind:item="item" v-bind:headers="headers">
                            {{ item[h.value] }}
                        </slot>
                    </td>
                </tr>
            </template>
        </v-data-table>
        <v-alert v-if="httpError" type="error" color="error" border="left" colored-border>
            <b>Oopsi!</b> Suddenly we could not fetch the data for you, try again later!
        </v-alert>
    </div>
</template>

<script>
    import axios from 'axios';

    export default {
        props: ['url', 'headers'],
        data: () => ({
            items: [],
            httpError: null,
        }),
        methods: {
            refreshData() {
                this.httpError = null;
                axios.get(this.url).then(response => this.items = response.data).catch(() => this.httpError = true);
            }
        },
        mounted() {
            this.refreshData();
        }
    }
</script>