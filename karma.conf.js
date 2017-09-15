/**
 * Copyright (c) 2015, CodiLime Inc.
 */

'use strict';

module.exports = function (config) {
    var settings = require('./config.json');
    var istanbul = require('browserify-istanbul');
    var params = {
        basePath: './',

        plugins: [
            'karma-chrome-launcher',
            'karma-jasmine',
            'karma-coverage',
            'karma-browserify',
            'browserify-istanbul',
            'karma-phantomjs-launcher'
        ],

        files: [
            './node_modules/angular/angular.js',
            './node_modules/lodash/index.js',
            './src/**/*.js'
        ],

        autoWatch: true,

        frameworks: ['browserify', 'jasmine'],

        browsers: ['PhantomJS'],

        reporters: ['progress','coverage'],

        preprocessors: {
            'src/**/*.js': ['browserify','coverage']
        },

        browserify: {
            debug: true,
            transform: ['browserify-shim', 'babelify','browserify-istanbul']
        },

        junitReporter: {
            outputFile: 'test_out/unit.xml',
            suite: 'unit'
        },

        coverageReporter: {
            reporters : [
                {"type": "text"},
                {"type": "html", dir: 'coverages'}
            ]
        }
    };

    params.preprocessors['**/*.spec.js'] = ['browserify'];
    config.set(params);
};
