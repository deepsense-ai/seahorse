/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
/*global console*/
'use strict';

var http     = require('http');
var Q        = require('q');
var fs       = require('fs');
var express  = require('express');
var app      = express();
var http     = require('http').Server(app);
var bodyParser = require('body-parser');

var httpProxy = require('http-proxy');
var proxies = [];

var settingsFile = 'settings.json';

var config = require('./../package.json');

Q.nfcall(fs.readFile, __dirname + '/' + settingsFile, 'utf-8')
 .catch(function() {
  console.log('Error reading settings file');
 })
 .done(function (text) {
  var settings;
  try {
    settings = JSON.parse(text);
  } catch(e) {
    console.log(settingsFile + ': ' + e);
    return;
  }

  if(settings.targets) {
    settings.targets.forEach(function(options) {
      var proxy = {
        proxy: httpProxy.createProxyServer({}),
        settings: options
      };

      var url = '/' + options.prefix;
      app.use(url, function(req, res, next) {
        req.url = req.url.replace(url, '');
        proxy.proxy.web(req, res, proxy.settings);
      });

      proxy.proxy.on('error', function (err, req, res) {
        res.writeHead(500, {
          'Content-Type': 'text/plain'
        });
        res.end('500');
      });

      proxies.push(proxy);
    });
  }

  require('./orm.js')(function(waterline) {
      app.use(bodyParser.urlencoded({ extended: true }));
      app.use(bodyParser.json());
      app.use('/apimock', require('./mock.js'));
      app.use('/api', require('./rest.js')(waterline));
      app.use('/', express.static(__dirname + '/../build'));

      http.listen(config.env.dev.port);
  });
});
