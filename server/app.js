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
  console.log('log:', 'using custom api host (' + apiConfig.url + ')');

  for (let resource in apiConfig.resources) {
    if (args['host-' + resource]) {
      apiConfig.resources[resource].url = args['host-' + resource];
    }
  }
}

// TODO: remove after removing deploy mock
let Q = require('q');
apiConfig.resources.models = {
  'deployURL': 'http://localhost:3000/webservice/regression/',
  'handler': (data, request) => {
    let deferred = Q.defer();
    data.link = apiConfig.resources.models.deployURL + data.id;
    deferred.resolve(data);
    return deferred.promise;
  }
};
if (args.deployURL) {
  apiConfig.resources.models.deployURL = args.deployURL;
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
