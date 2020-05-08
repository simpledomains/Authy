import Vue from 'vue'
import store from './plugins/store'
import router from './plugins/router'
import vuetify from './plugins/vuetify';
import App from './App.vue'

import 'sweetalert2/dist/sweetalert2.css';

Vue.config.productionTip = false;

Vue.prototype.$version = process.env.VUE_APP_CI_COMMIT_SHORT_SHA || 'development';
Vue.prototype.$testMode = process.env.VUE_APP_TEST_MODE === 'true' || false;
Vue.prototype.$store = store;

new Vue({
    store: store,
    router: router,
    vuetify: vuetify,
    el: '#app',
    render: render => render(App)
});