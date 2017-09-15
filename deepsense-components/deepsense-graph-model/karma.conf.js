/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by Grzegorz Swatowski
 */

'use strict';

module.exports = function (config) {
  var params = {
    basePath: './',

    plugins: [
      'karma-jasmine',
      'karma-phantomjs-launcher',
      'karma-babel-preprocessor'
    ],

    files: [
      './node_modules/angular/angular.js',
      './node_modules/angular-mocks/angular-mocks.js',
      './node_modules/lodash/index.js',
      '../deepsense-node-parameters/dist/deepsense-node-parameters.js',
      './src/deepsense-graph-model.module.js',
      './src/**/*.js',
      './test/**/*.spec.js'
    ],

    autoWatch: true,

    frameworks: ['jasmine'],

    browsers: ['PhantomJS'],

    preprocessors: {
      'src/**/*.js': ['babel'],
      'test/**/*.spec.js': ['babel']
    }
  };

  config.set(params);
};
