import Vue from 'vue';
import Vuetify from 'vuetify/lib';
import '@fortawesome/fontawesome-free/css/all.css' // Ensure you are using css-loader
import * as directives from 'vuetify/es5/directives';
import colors from 'vuetify/lib/util/colors'

Vue.use(Vuetify, {directives});

export default new Vuetify({
    theme: {
        options: {
            customProperties: true,
        },
        dark: false,
        themes: {
            dark: {
                'service-card': colors.grey.darken3,
                'service-card-roles': colors.green.darken3,
                'service-card-urls': colors.blue.darken3,
                primary: '#1A237E',
                accent: '#FF4081',
                secondary: '#ffe18d',
                success: '#469349',
                info: '#2196F3',
                warning: '#FB8C00',
                error: '#FF5252'
            },
            light: {
                'service-card': colors.grey.lighten3,
                'service-card-roles': colors.green.lighten3,
                'service-card-urls': colors.blue.lighten3,
                primary: '#1976D2',
                accent: '#e91e63',
                secondary: '#30b1dc',
                success: '#4CAF50',
                info: '#2196F3',
                warning: '#FB8C00',
                error: '#FF5252'
            }
        }
    },
    icons: {
        iconfont: 'mdi',
    },
});