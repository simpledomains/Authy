import Vue from 'vue'
import Router from 'vue-router'
import Login from '../views/Login'
import Profile from "../views/Profile";
import Logout from "../views/Logout";
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
            props: {cas: false},
            meta: {
                unauthenticated: true
            }
        },
        {
            path: '/cas/login',
            name: 'CasLogin',
            component: Login,
            props: {cas: true},
            meta: {
                unauthenticated: true
            }
        },
        {
            path: '/logout',
            name: 'Logout',
            component: Logout,
            meta: {
                authenticated: true
            }
        },
        {
            path: '/admin/test',
            component: () => {
            },
            meta: {
                admin: true
            }
        }
    ]
});

router.beforeEach((to, from, next) => {
    if (to.matched.some(r => r.meta.authenticated)) {
        if (Vue.prototype.$store.state.authenticationToken === "") {
            console.log("Redirecting to /login, this endpoint cannot be access without login.");
            next({
                path: '/login',
                query: {service: '/'}
            });
        } else {
            next();
        }
    } else if (to.matched.some(r => r.meta.unauthenticated)) {
        if (Vue.prototype.$store.state.authenticationToken !== "") {
            console.log("Redirecting back to old route, cause this is not allowed authenticated.");
            next(from);
        } else {
            next();
        }
    } else if (to.matched.some(r => r.meta.admin)) {
        if (Vue.prototype.$store.state.admin) {
            next();
        } else {
            console.log("Access to admin route was denied.");
            next(from);
        }
    } else {
        next();
    }
});

export default router;