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

'use strict';

const path = require('path');
const webpack = require('webpack');
const autoprefixer = require('autoprefixer-core');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const GitRevisionPlugin = require('git-revision-webpack-plugin');

const NODE_ENV = process.env.NODE_ENV || 'production';

module.exports = function (_path) {
  const webpackConfig = {
    entry: {
      libs: _path + '/client/app/libs.js', // TODO: remove
      app: _path + '/client/app/app.js',
      ga: _path + '/client/app/app.ga.js'
    },

    output: {
      path: 'dist',
      filename: '[name].js',
      publicPath: '/'
    },

    // resolves modules
    resolve: {
      extensions: ['', '.js'],
      modulesDirectories: ['node_modules'],
      alias: {
        APP: path.join(_path, 'client', 'app'),
        ASSETS: path.join(_path, 'client', 'assets'),
        COMMON: path.join(_path, 'client', 'common'),
        COMPONENTS: path.join(_path, 'client', 'components'),
        LESS: path.join(_path, 'client', 'less'),
        NODE_MODULES: path.join(_path, 'node_modules'),
        SRC: path.join(_path, 'client'),
        STATIC: path.join(_path, 'client', 'static'),
        VENDOR: path.join(_path, 'vendor'),
        _styles: path.join(_path, 'client', 'css')
      }
    },

    eslint: {
      configFile: path.join(_path, 'config', 'eslint', 'eslint-src.config.js')
    },

    module: {
      preLoaders: [{
        test: /\.js$/,
        loader: 'eslint-loader',
        exclude: [
          /node_modules/,
          /vendor/
        ]
      }],
      noParse: [],
      loaders: [
      {
        test: /\.json$/,
        loader: 'json-loader'
      }, {
        test: /\.html$/,
        loaders: [
          'ngtemplate-loader?relativeTo=' + _path,
          'html?-minimize'
        ]
      }, {
        test: /\.css$/,
        loader: 'style-loader!css-loader!postcss-loader'
      }, {
        test: /\.less/,
        loader: 'style-loader!css-loader!postcss-loader!less-loader'
      }, {
        test: /\.(png|jpg|gif)$/,
        loader: 'url-loader?limit=8192'
      }, {
        test: /\.woff(2)?(\?v=[0-9]\.[0-9]\.[0-9])?$/,
        loader: 'url-loader?limit=10000&mimetype=application/font-woff'
      }, {
        test: /\.(ttf|eot|svg)(\?v=[0-9]\.[0-9]\.[0-9])?$/,
        loader: 'url-loader'
      }, {
        test: /\.js$/,
        exclude: [
          path.resolve(_path, 'node_modules')
        ],
        loaders: [
          'ng-annotate-loader'
        ]
      }, {
        test: /\.js$/,
        loader: 'babel-loader',
        exclude: [
          path.resolve(_path, 'node_modules')
        ],
        query: {
          cacheDirectory: true,
          plugins: [
            'transform-runtime',
            'add-module-exports'
          ]
        }
      }, {
        test: require.resolve('angular'),
        loaders: [
          'expose?angular'
        ]
      }, {
        test: require.resolve('jquery'),
        loaders: [
          'expose?$',
          'expose?jQuery'
        ]
      }

      ]
    },

    // For SockJs
    node: {
      net: 'empty',
      tls: 'empty',
      dns: 'empty'
    },

    postcss: [autoprefixer({browsers: ['last 5 versions']})],

    plugins: [
      // new webpack.ContextReplacementPlugin(/moment[\/\\]locale$/, /en|hu/),
      new webpack.ProvidePlugin({
        $: 'jquery',
        jQuery: 'jquery'
      }),
      new webpack.DefinePlugin({
        'NODE_ENV': JSON.stringify(NODE_ENV)
      }),
      new webpack.NoErrorsPlugin(),
      new webpack.IgnorePlugin(/^\.\/locale$/, /moment$/),
      new webpack.optimize.DedupePlugin(),
      new webpack.optimize.AggressiveMergingPlugin({
        moveToParents: true
      }),
      new webpack.optimize.CommonsChunkPlugin({
        name: 'common',
        minChunks: 2
      }),
      new HtmlWebpackPlugin({
        favicon: path.join(_path, 'client', 'favicon.ico'),
        filename: 'index.html',
        template: path.join(_path, 'client', 'index.html'),
        gitHash: JSON.stringify(new GitRevisionPlugin().commithash())
      })
    ]
  };

  return webpackConfig;
};
