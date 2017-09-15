'use strict';

const global = require('./global.js');
const config = global(__dirname);

module.exports = Object.assign(config, {
  module: {
    loaders: [
      {
        test: /.json$/,
        loaders: [
          'json-loader'
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
});
