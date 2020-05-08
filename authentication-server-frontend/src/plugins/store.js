import Vue from 'vue'
import Vuex from 'vuex'
import VuexPersistence from "vuex-persist";

Vue.use(Vuex)

const vuexPersistence = new VuexPersistence({
    key: 'AuthyWebApp',
    storage: window.sessionStorage,
})

const store = new Vuex.Store({
    state: {
        authenticationToken: "",
        admin: false,
        uid: null,
        username: null,
        darkMode: false,
    },
    mutations: {
        setToken(state, token) {
            let data = token.split(".");
            let identity = JSON.parse(atob(data[1]));

            state.authenticationToken = token
            state.admin = identity["administrator"] === 'true';
            state.uid = identity["uid"];
            state.username = identity["sub"]
        },
        removeToken(state) {
            state.authenticationToken = '';
            state.admin = false;
            state.uid = null;
            state.username = null;
        },
        toggleDarkMode(state) {
            state.darkMode = !state.darkMode;
        }
    },
    plugins: [vuexPersistence.plugin]
})

export default store;