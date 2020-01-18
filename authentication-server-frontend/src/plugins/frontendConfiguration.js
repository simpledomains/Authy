import axios from 'axios';

let processConfiguration = (vuetify, cfg) => {
    vuetify.theme.dark = cfg.darkMode;
};

let process = function (vuetify) {

    let config = localStorage.getItem('AuthyConfig');

    if (config == null) {
        axios.get('/api/frontend/config').then(r => {

            let data = r.data;
            data.expire = ((new Date().getTime() / 1000) + (60 * 60 * 24)) * 1000;

            localStorage.setItem('AuthyConfig', JSON.stringify(data));
            processConfiguration(vuetify, data);
        })
    } else {
        let parsed = JSON.parse(config);

        if (parsed.expire < new Date().getTime()) {
            localStorage.removeItem('AuthyConfig');
            process(vuetify);
        } else {
            processConfiguration(vuetify, JSON.parse(config));
        }
    }
};

export default process;