import Vue from 'vue'
import Router from 'vue-router'
import Login from '../views/Login'
import Profile from "../views/Profile";
//import AccessDenied from "../views/AccessDenied";
//import CasError from "../views/CasError";
//import RouteOverview from "../views/admin/RouteOverview";
//import UserOverview from "../views/admin/UserOverview";

Vue.use(Router);

let router = new Router({
    routes: [
        {
            path: '/',
            name: 'Profile',
            component: Profile,
            meta: {
                authenticated: true,
            }
        },
        {
            path: '/login',
            name: 'Login',
            component: Login,
            props: {cas: false}
        },
        {
            path: '/cas/login',
            name: 'CasLogin',
            component: Login,
            props: {cas: true}
        },
        {
            path: '/logout',
            name: 'Logout',
            component: {
                mounted() {
                    this.$store.commit('removeToken');
                    this.$router.push({path: '/login', query: {service: '/'}});
                }
            },
        },
        {
            path: '/admin/test',
            component: () => {
            },
            meta: {
                authenticated: true
            }
        }
    ]
})

router.beforeEach((to, from, next) => {
    if (to.matched.some(r => r.meta.authenticated)) {
        if (Vue.prototype.$store.state.authenticationToken === "") {
            next({
                path: '/login',
                query: {service: '/'}
            });
        } else {
            next();
        }
    } else {
        next();
    }
})

export default router;