import Vue from 'vue'
import Vuex from 'vuex'
import VuexPersistence from "vuex-persist";

Vue.use(Vuex)

const vuexPersistence = new VuexPersistence({
    key: 'AuthyLocalStorage',
    storage: window.localStorage,
})

const store = new Vuex.Store({
    state: {
        dismissed: {},
    },
    mutations: {
        dismiss(state, id) {
            state.dismissed[id] = true;
        }
    },
    plugins: [vuexPersistence.plugin]
})

export default store;