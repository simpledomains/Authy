import Vue from 'vue'
import router from './plugins/router'
import vuetify from './plugins/vuetify';
import frontendConfiguration from './plugins/frontendConfiguration';

import 'sweetalert2/dist/sweetalert2.css';

Vue.config.productionTip = false;

Vue.prototype.$version = process.env.VUE_APP_CI_COMMIT_SHORT_SHA || 'local-snapshot';
Vue.prototype.$frontendConfiguration = frontendConfiguration;
Vue.prototype.$testMode = process.env.VUE_APP_TEST_MODE === 'true' || false;

new Vue({
    router,
    vuetify,
    el: '#app',
});