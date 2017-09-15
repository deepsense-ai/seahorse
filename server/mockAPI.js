/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
'use strict';

var fs     = require('fs');
var url    = require('url');

module.exports = function(req, res, next) {
  var uri = url.parse(req.url).pathname.replace(/\/$/, '');

  var p = '/fixtures/' + uri.replace(/\//g,'.').substr(1) + '.json';
  var filename = __dirname + p;

  var readStream = fs.createReadStream(filename);
  readStream.on('open', function () {
    readStream.pipe(res);
  });

  readStream.on('error', function(err) {
    res.writeHead(404, {'Content-Type':'text/plain'});
    res.end('404 - missing (mock data)');
  });
};
