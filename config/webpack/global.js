'use strict';

const path = require('path');
const webpack = require('webpack');
const autoprefixer = require('autoprefixer-core');
const HtmlWebpackPlugin = require("html-webpack-plugin");
const GitRevisionPlugin = require('git-revision-webpack-plugin');

const NODE_ENV = process.env.NODE_ENV || "production";

module.exports = function (_path) {
  const rootAssetPath = _path + 'client';

  const webpackConfig = {
    entry: {
      libs: _path + '/client/app/libs.js', //TODO: remove
      app: _path + '/client/app/app.js',
      fe: _path + '/client/app/app.fe.js',
      ga: _path + '/client/app/app.ga.js',
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
        _appRoot: path.join(_path, 'client', 'app'),
        _fonts: path.join(_path, 'client', 'fonts'),
        _images: path.join(_path, 'client', 'img'),
        _static: path.join(_path, 'client', 'static'),
        _styles: path.join(_path, 'client', 'css')
      }
    },

    module: {
      preLoaders: [ {
        test: /\.js$/, loader: "eslint-loader", exclude: /node_modules/
      }
      ],
      noParse: [],
      loaders: [{
        //TODO: jsPlumb issue: https://github.com/sporritt/jsPlumb/issues/314
        test: require.resolve('jsplumb'),
        loaders: [
          'imports?this=>window',
          'script'
        ]
      },{
        test: /\.json$/,
        loader: 'json-loader'
      }, {
        test: /\.html$/,
        loaders: [
          'ngtemplate-loader?relativeTo=' + _path,
          'html-loader?attrs[]=img:src&attrs[]=img:data-src'
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
        loader: "url-loader?limit=10000&mimetype=application/font-woff"
      }, {
        test: /\.(ttf|eot|svg)(\?v=[0-9]\.[0-9]\.[0-9])?$/,
        loader: "url-loader"
      }, {
        test: /\.js$/,
        exclude: [
          path.resolve(_path, "node_modules")
        ],
        loaders: [
          'ng-annotate-loader'
        ]
      }, {
        test: /\.js$/,
        loader: 'babel-loader',
        exclude: [
          path.resolve(_path, "node_modules")
        ],
        query: {
          cacheDirectory: true,
          plugins: [
            "transform-runtime",
            "add-module-exports"
          ]
        }
      }, {
        test: require.resolve("angular"),
        loaders: [
          "expose?angular"
        ]
      }, {
        test: require.resolve("jquery"),
        loaders: [
          "expose?$",
          "expose?jQuery"
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
      //new webpack.ContextReplacementPlugin(/moment[\/\\]locale$/, /en|hu/),
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
        filename: 'index.html',
        template: path.join(_path, 'client', 'index.html'),
        gitHash: JSON.stringify(new GitRevisionPlugin().commithash())
      })
    ]
  };

  if (NODE_ENV !== 'development') {
    webpackConfig.plugins = webpackConfig.plugins.concat([
      new webpack.optimize.UglifyJsPlugin()
    ]);
  }

  return webpackConfig;
};
