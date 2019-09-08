<template>
    <v-row>
        <v-flex lg6 pl-3 pr-3>
            <v-card-text>
                <v-icon small right left>{{ icon || 'fas fa-signature' }}</v-icon>
                {{ name || 'Unknown Field' }}
            </v-card-text>
        </v-flex>
        <v-flex lg6 pl-3 pr-3>
            <v-text-field
                    v-if="!disabled"
                    :value="value"
                    :success-messages="original !== value ? 'Modified (Press \'Enter\' to save)' : ''"
                    @input="updateValue"
                    @keydown.enter="submitData"
                    append-icon="fas fa-save"
                    @click:append="submitData"/>

            <v-text-field
                    v-if="disabled"
                    :disabled="true"
                    :value="value"
                    @input="updateValue"
                    append-icon="fas fa-save-alt"
                    @keydown.enter="submitData"/>
        </v-flex>
    </v-row>
</template>

<script>
    export default {
        props: {
            value: String,
            name: String,
            icon: String,
            disabled: Boolean
        },


        data() {
            return {
                original: null,
            }
        },

        mounted() {
            this.original = this.value;
        },

        methods: {
            submitData() {
                if (this.original !== this.value) {
                    this.$emit('submit', this.value);
                    this.$emit('input', this.value);
                    this.original = this.value;
                }
            },
            updateValue(ev) {
                this.$emit('input', ev);
            }
        }
    }
</script>