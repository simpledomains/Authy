import Vue from 'vue';
import Vuetify from 'vuetify/lib';
import '@fortawesome/fontawesome-free/css/all.css' // Ensure you are using css-loader
import colors from "vuetify/lib/util/colors";
import * as directives from 'vuetify/es5/directives';

Vue.use(Vuetify, {directives});

export default new Vuetify({
    theme: {
        dark: false,
        themes:{
            dark: {
                primary: colors.indigo,
            },
            light: {
                primary: colors.indigo,
            }
        }
    },
    icons: {
        iconfont: 'mdi',
    },
});