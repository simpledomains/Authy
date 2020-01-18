import Vue from 'vue';
import Vuetify from 'vuetify/lib';
import '@fortawesome/fontawesome-free/css/all.css' // Ensure you are using css-loader
import * as directives from 'vuetify/es5/directives';

Vue.use(Vuetify, {directives});

export default new Vuetify({
    theme: {
        dark: false,
        themes: {
            dark: {
                primary: '#1A237E',
                accent: '#FF4081',
                secondary: '#ffe18d',
                success: '#4CAF50',
                info: '#2196F3',
                warning: '#FB8C00',
                error: '#FF5252'
            },
            light: {
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