/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
