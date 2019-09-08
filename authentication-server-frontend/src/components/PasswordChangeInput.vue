<template>
    <div>
        <v-row>
            <v-flex lg6 pl-3 pr-3>
                <v-card-text>
                    <v-icon small right left>fas fa-lock</v-icon>
                    Password
                </v-card-text>
            </v-flex>
            <v-flex lg6 pl-3 pr-3>
                <v-text-field
                        label="Fill in to change your password!"
                        v-model="newPassword"
                        :error="error"
                        :error-messages="errors"
                        :error-count="errors.length"
                        :type="show ? 'text' : 'password'"
                        :append-icon="show ? 'far fa-eye-slash' : 'far fa-eye'"
                        @click:append="toggleShow"
                        @input="updateValue"
                        @keydown.enter="submitData"/>
            </v-flex>
        </v-row>
        <v-expand-transition>
            <v-row v-show="newPassword.length > 0 && !show" class="pb-4">
                <v-flex lg6 pl-3 pr-3>
                    <v-card-text>
                        <v-icon small right left>fas fa-lock</v-icon>
                        Repeat Password
                    </v-card-text>
                </v-flex>
                <v-flex lg6 pl-3 pr-3>
                    <v-text-field
                            label="Repeat the password"
                            type="password"
                            v-model="repeatedPassword"
                            :success="errors.length === 0"
                            :success-messages="errors.length === 0 ? 'Press \'Enter\' to save this password.' : ''"
                            :error="error"
                            :error-count="errors.length"
                            @input="updateValue"
                            @keydown.enter="submitData"/>
                </v-flex>
            </v-row>
        </v-expand-transition>
    </div>
</template>

<script>
    export default {
        props: {
            disabled: Boolean
        },


        data() {
            return {
                error : false,
                errors: [],

                show: false,

                newPassword     : '',
                repeatedPassword: '',
            }
        },

        methods: {
            passwordMatches() {
                if (this.repeatedPassword !== this.newPassword && !this.show) {
                    this.error = true;
                    this.errors.push('Passwords does not match');
                }
            },
            passwordIsSecure() {
                this.textContainsMatchesXTimes(this.newPassword, "[A-Z]", 1, 'Must have at least 1 upper cased letters.');
                this.textContainsMatchesXTimes(this.newPassword, "[a-z]", 3, 'Must have at least 3 lower cased letters.');
                this.textContainsMatchesXTimes(this.newPassword, "[0-9]", 1, 'Must have at least 1 numeric.');
                this.textContainsMatchesXTimes(this.newPassword, "[!@#§$%&/()=?^-_+]", 1, 'Must have at least 1 of (!@#§$%&/()=?^-_+).');
            },

            textContainsMatchesXTimes(str, pattern, times, errorMessage) {
                let output = [...str.matchAll(pattern)];

                if (output) {
                    if (output.length < (times)) {
                        this.error = true;
                        this.errors.push(errorMessage);
                    }
                } else {
                    this.error = true;
                    this.errors.push(errorMessage);
                }
            },

            submitData() {
                if (this.errors.length === 0) {
                    this.$emit('submit', this.newPassword);
                    this.newPassword      = '';
                    this.repeatedPassword = '';
                }
            },
            updateValue() {
                this.error  = false;
                this.errors = [];

                if (this.newPassword === '') this.repeatedPassword = '';
                else {
                    this.passwordMatches();
                    this.passwordIsSecure();
                }
            },
            toggleShow() {
                this.show = !this.show;
                this.updateValue();
            }
        }
    }
</script>