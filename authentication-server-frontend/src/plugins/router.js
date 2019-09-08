import Vue from 'vue'
import Router from 'vue-router'
import Login from '../views/Login'
import Home from "../views/Home";
import AccessDenied from "../views/AccessDenied";
import Users from "../views/admin/Users";
import Routes from "../views/admin/Routes";
import CasError from "../views/CasError";

Vue.use(Router);

export default new Router({
    routes: [
        {
            path     : '/',
            name     : 'Home',
            component: Home
        },
        {
            path     : '/login',
            name     : 'Login',
            component: Login,
            props: { cas: false }
        },
        {
            path     : '/cas/login',
            name     : 'CasLogin',
            component: Login,
            props: { cas: true }
        },
        {
            path     : '/cas/error',
            name     : 'CasError',
            component: CasError
        },
        {
            path     : '/access-denied',
            name     : 'AccessDenied',
            component: AccessDenied
        },
        {
            path     : '/admin/users',
            name     : 'UserAdmin',
            component: Users
        },
        {
            path     : '/admin/routes',
            name     : 'RouteAdmin',
            component: Routes
        },
    ]
})
