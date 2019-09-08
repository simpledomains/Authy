import Vue from 'vue'
import router from './plugins/router'
import vuetify from './plugins/vuetify';

Vue.config.productionTip = false;

new Vue({
    router,
    vuetify,
    el: '#app'
});
