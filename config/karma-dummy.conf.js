'use strict';

module.exports = (config) => {
  config.set({
    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: false,

    // base path that will be used to resolve all patterns (eg. files, exclude)
    basePath: '..',

    // start these browsers
    // available browser launchers: https://npmjs.org/browse/keyword/karma-launcher
    browsers: ['PhantomJS'],

    // list of files to exclude
    exclude: [],

    // list of files / patterns to load in the browser
    files: [
      'client/index.spec.js'
    ],

    // frameworks to use
    // available frameworks: https://npmjs.org/browse/keyword/karma-adapter
    frameworks: ['jasmine'],

    // List of plugins to load
    plugins: [
      'karma-chrome-launcher',
      'karma-jasmine',
      'karma-phantomjs-launcher',
      'karma-webpack'
    ],

    // preprocess matching files before serving them to the browser
    // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
    preprocessors: {
      'client/**/*.spec.js': ['webpack']
    },

    webpack: {
      module: {
        preLoaders: [
          {
            test: /\.js$/,
            exclude: /node_modules/,
            loader: 'eslint'
          }
        ],

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
              'babel'
            ]
          },
          {
            test: /.html$/,
            loaders: [
              'html'
            ]
          }
        ]
      },
      plugins: [],
      debug: true,
      devtool: 'cheap-source-map'
    },

    // Continuous Integration mode
    // if true, Karma captures browsers, runs the tests and exits
    singleRun: true
  });
};
