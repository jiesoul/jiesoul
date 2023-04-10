module.exports = {
    // plugins: [
    //     require('postcss-import'),
    //     require('postcss-nesting'),
    //     require('tailwindcss/nesting'),
    //     require('tailwindcss'),
    //     require('autoprefixer'),
    // ]

    plugins: {
        tailwindcss: {},
        autoprefixer: {},
        cssnano: process.env.NODE_ENV == 'production' ? {} : false
    }

}