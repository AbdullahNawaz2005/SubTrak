// SubTrak Frontend Configuration
// Change API_BASE to your deployed backend URL in production
const CONFIG = {
    // Auto-detect: use production URL if not on localhost
    API_BASE: window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1'
        ? 'http://localhost:8080'
        : 'https://subtrak-api.onrender.com',
    
    // App info
    APP_NAME: 'SubTrak',
    VERSION: '1.0.0'
};

// Make it globally available
window.SUBTRAK_CONFIG = CONFIG;
