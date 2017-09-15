/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
'use strict';

var express = require('express'),
    app = express(),
    http = require('http').Server(app),
    config = require('./../package.json'),
    apiConfig = require('./api/apiConfig'),
    experimentHandler = require('./api/experimentHandler.js');


// TODO: remove after full login implementation
let args = require('minimist')(process.argv.slice(2));
if (args.host && args.token) {
  apiConfig.url = args.host;
  apiConfig.token = args.token;
  console.log('using custom api host (' + apiConfig.url + ')');
}


require('./api/ormHandler.js')((orm) => {
  apiConfig.localDB = orm;

  // mock
  require('./mocks/mockDB.js')(apiConfig);
  app.use('/apimock/v1', require('./mocks/mockAPI.js'));

  // api proxy
  apiConfig = experimentHandler(apiConfig);
  app.use('/api', require(__dirname + '/api/apiProxy.js')(apiConfig));

  // lab pages
  app.use('/', express.static(__dirname + '/../build'));

  http.listen(config.env.dev.port);
});
