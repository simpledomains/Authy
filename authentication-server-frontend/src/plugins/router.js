import Vue from 'vue'
import Router from 'vue-router'
import Login from '../views/Login'
import Profile from "../views/Profile";
import Logout from "../views/Logout";
import AdminUsers from "../views/AdminUsers";
import AdminUsersCreate from "../views/AdminUsersCreate";
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
            path: '/admin/users',
            component: AdminUsers,
            meta: {
                admin: true
            }
        },
        {
            path: '/admin/users/create',
            component: AdminUsersCreate,
            meta: {
                admin: true
            }
        }
    ]
});

router.beforeEach((to, from, next) => {
    let requiresAuth = to.matched.some(r => r.meta.authenticated);
    let requiresAdmin = to.matched.some(r => r.meta.admin);
    let requiresAnon = to.matched.some(r => r.meta.unauthenticated);
    let isSignedIn = Vue.prototype.$store.state.authenticationToken !== "";
    let isAdmin = isSignedIn && Vue.prototype.$store.state.admin;

    if ((requiresAdmin || requiresAuth) && !isSignedIn) { // AUTH REQUIRED
        console.log("[ROUTER] Authentication is required. (path=" + to.path + ")");
        next({path: '/login', query: {service: '/'}})
    } else if (requiresAdmin && !isAdmin) {// ADMIN REQUIRED
        console.log("[ROUTER] Access Denied. (path=" + to.path + ")");
        next(from);
    } else if (requiresAnon && isSignedIn) {
        console.log("[ROUTER] Authentication is not allowed here. (path=" + to.path + ")");
        next(from);
    } else {
        next();
    }
});

export default router;