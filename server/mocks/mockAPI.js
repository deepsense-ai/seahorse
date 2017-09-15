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

  // redirect experiment/:id/action response to experiment data
  uri = uri.replace(/\/action$/, '');

  var p = '/fixtures/' + uri.replace(/\//g,'.').substr(1) + '.json';
  var filename = __dirname + p;

  var readStream = fs.createReadStream(filename),
      data = '';

  readStream.on('data', function (source) {
    data += source;
  });

  readStream.on('end', function () {
    res.writeHead(200, {
      'Content-Type': 'application/json; charset=UTF-8',
      'Content-Length': data.length
    });
    res.write(data);
    res.end();
  });

  readStream.on('error', function(err) {
    res.writeHead(404, {'Content-Type': 'text/plain; charset=UTF-8'});
    res.end('404 - missing (mock data)');
  });
};
