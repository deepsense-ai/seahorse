/**
 * Copyright (c) 2015, CodiLime Inc.
 */

'use strict';

module.exports = function(config) {
  var settings = require('./config.json');
  var params = {
      basePath: './',

      files: [
        './node_modules/angular/angular.js',
        './node_modules/lodash/index.js',
        './src/**/*.spec.js'
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

  params.preprocessors['**/*.spec.js'] = ['browserify'];
  config.set(params);
};
