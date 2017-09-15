'use strict';

module.exports = function(config) {
  var settings = require('./config.json');
  var params = {
      basePath: './',

      files: [
        './node_modules/angular/angular.js',
        './node_modules/angular-ui-router/release/angular-ui-router.js',
        './node_modules/angular-ui-bootstrap/dist/ui-bootstrap.js',
        './node_modules/angular-ui-bootstrap/dist/ui-bootstrap-tpls.js',
        './node_modules/angular-mocks/angular-mocks.js',
        './bower_components/lodash/lodash.min.js',
        './node_modules/angular-toastr/dist/angular-toastr.tpls.min.js',
        './node_modules/stompjs/lib/stomp.js',
        './node_modules/sockjs-client/lib/bundle.js',
        settings.files.tests.client
      ],

      autoWatch: true,
      singleRun: false,

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
