const defaultTheme = require('tailwindcss/defaultTheme')

module.exports = {
    content: ['./public/js/*.js'],
    theme: {
        screens: {
            sm: '480px',
            md: '768px',
            lg: '976px',
            xl: '1440px'
        },
        
        extend: {
            '128': '32rem',
            '144': '36rem',
        },
        borderRadius: {
            '4xl': '2rem',
          }
    },
    variants: {},
    plugins: [
        require('@tailwindcss/forms'),
    ],
}