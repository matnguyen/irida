module.exports = function (config) {
    var app_path = 'src/main/webapp',
        test_path = 'src/test/javascript';
    config.set({
        files: [
                app_path + '/static/scripts/*.js',
                app_path + '/bower_components/angular-mocks/angular-mocks.js',
                test_path + '/unit/**/*spec.js'
        ],
        basePath: '',
        frameworks: ['jasmine'],
        reporters: ['progress'],
        browsers: ['Chrome', 'Firefox',],
        autoWatch: true,
        singleRun: false,
        colors: true
    });
};