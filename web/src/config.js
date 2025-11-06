// config.js
export default {
    webRTCServerUrl: process.env.NODE_ENV === 'production'
        ? 'http://192.168.31.180:8000'
        : 'http://192.168.31.180:8000',
};