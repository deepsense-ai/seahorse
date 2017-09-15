'use strict';

module.exports = function (config) {
  const params = {
    basePath: '..',

    files: [
      './node_modules/angular/angular.js',
      './node_modules/angular-mocks/angular-mocks.js',
      './node_modules/lodash/lodash.min.js',
      './client/app/**/*.spec.js'
    ],

    autoWatch: true,

    singleRun: true,

    frameworks: ['jasmine'],

    browsers: ['PhantomJS'],

    plugins: [
      'karma-webpack',
      'karma-chrome-launcher',
      'karma-jasmine',
      'karma-phantomjs-launcher'
    ],

    // preprocess matching files before serving them to the browser
    // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
    preprocessors: {
      'client/**/*.spec.js': ['webpack']
    },

    junitReporter: {
      outputFile: 'test_out/unit.xml',
      suite: 'unit'
    },

    webpack: {
      module: {
        loaders: [
          {
            test: /.json$/,
            loaders: [
              'json'
            ]
          },
          {
            test: /\.js$/,
            exclude: /node_modules/,
            loaders: [
              'ng-annotate',
              'babel-loader'
            ]
          },
          {
            test: /.html$/,
            loaders: [
              'html'
            ]
          },
        ]
      },
      plugins: [],
      debug: true,
      devtool: 'cheap-source-map'
    }
  };

  config.set(params);
};
