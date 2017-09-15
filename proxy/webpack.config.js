const nodeExternals = require('webpack-node-externals');
const webpack = require('webpack');
const CopyWebpackPlugin = require('copy-webpack-plugin');

module.exports = {
  entry: "./app/server/server.js",
  output: {
    path: __dirname + "/dist/",
    filename: "bundle.js"
  },
  target: "node",
  externals: [
    // Module 'pkginfo' makes assumptions about its directory ($PROJECT/node_modules/pkginfo)
    // and on __dirname variable. If it gets bundled inside final js it won't work.
    // Thats why we are excluding node_modules and bundle only our code.
    [nodeExternals()]
  ],
  module: {
    loaders: [
      {test: /\.json$/, loader: "json-loader"},
      {
        test: /\.js$/,
        exclude: /(node_modules|bower_components)/,
        loader: 'babel-loader',
        query: {
          presets: ['env']
        }
      }
    ]
  },
  plugins: [
    new webpack.optimize.UglifyJsPlugin({
      compress: {
        warnings: false
      }
    }),
    new CopyWebpackPlugin([
      { from: 'app/server/html', to: "app/server/html" },
    ])
  ]
};
