const webpack = require('webpack');

module.exports = {
    runtimeCompiler : true,
    devServer       : {
        proxy: {
            '^/api': {
                target      : 'http://localhost:3000',
                ws          : true,
                changeOrigin: true
            },
            '^/cas': {
                target      : 'http://localhost:3000',
                ws          : true,
                changeOrigin: true
            },
            '^/auth': {
                target      : 'http://localhost:3000',
                ws          : true,
                changeOrigin: true
            },
            '^/webauthn': {
                target      : 'http://localhost:3000',
                ws          : true,
                changeOrigin: true
            },
        }
    },
    configureWebpack: {
        plugins: [
            new webpack.IgnorePlugin(/^\.\/locale$/, /moment$/)
        ]
    }
};
