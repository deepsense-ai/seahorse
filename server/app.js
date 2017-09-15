/**
 * Copyright (c) 2015, CodiLime Inc.
 */
"use strict";

var http     = require('http');
var Q        = require('q');
var fs       = require('fs');
var express  = require('express');
var app      = express();
var http     = require('http').Server(app);
var Waterline = require('waterline');
var bodyParser = require('body-parser');

var httpProxy = require('http-proxy');
var proxies = [];

var settingsFile = "settings.json";
Q.nfcall(fs.readFile, __dirname + '/' + settingsFile, "utf-8")
 .catch(function() {
  console.log('Error reading settings file');
 })
 .done(function (text) {
  try {
    var settings = JSON.parse(text);
  } catch(e) {
    console.log(settingsFile + ": " + e);
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
      console.log('ORM ready');

      app.use(bodyParser.urlencoded({ extended: true }));
      app.use(bodyParser.json());
      app.use('/api', require('./rest.js')(waterline));
      app.use('/', express.static(__dirname + '/../build'));

      var port = 8000;
      http.listen(port);
      console.log("Listening on port " + port + ".");
  });
});
