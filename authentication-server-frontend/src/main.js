import Vue from 'vue'
import router from './plugins/router'
import vuetify from './plugins/vuetify';

import 'sweetalert2/dist/sweetalert2.css';

Vue.config.productionTip = false;

Vue.prototype.$version = process.env.VUE_APP_CI_COMMIT_SHORT_SHA;

new Vue({
    router,
    vuetify,
    el: '#app',
});