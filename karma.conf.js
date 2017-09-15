/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
'use strict';


module.exports = function(config) {
  var settings = require('./package.json'),
      params = {
        basePath: './',

        files: [
          './node_modules/angular/angular.js',
          './node_modules/angular-ui-router/release/angular-ui-router.js',
          './node_modules/angular-mocks/angular-mocks.js',
          './bower_components/lodash/lodash.min.js',
          settings.files.tests.client
        ],

        autoWatch: true,

        frameworks: ['browserify', 'jasmine'],

        browsers: ['PhantomJS'],

        plugins: [
          'karma-chrome-launcher',
          'karma-jasmine',
          'karma-browserify',
          'karma-phantomjs-launcher'
        ],

       preprocessors: {},

        browserify: {
          transform: ['browserify-shim', 'babelify']
        },

        junitReporter : {
          outputFile: 'test_out/unit.xml',
          suite: 'unit'
        }
      };

  params.preprocessors[settings.files.tests.client] = ['browserify'];
  config.set(params);
};
