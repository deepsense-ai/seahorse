/**
 * Copyright (c) 2016, CodiLime Inc.
 */
var request = require('request'),
    _ = require('underscore'),
    url = require('url'),
    util = require('util'),

    localServices = require('./config/local-services'),
    serviceMapping = require('./config/service-mapping'),
    config = require('./config/config'),
    httpException = require('./utils/http-exception'),
    gatewayErrors = require('./gateway-errors');

var basicAuthCredentials = new Buffer(
      config.get('WM_AUTH_USER') + ':' + config.get('WM_AUTH_PASS')
    ).toString('base64')

var httpProxy = require('http-proxy');
var proxy = httpProxy.createProxyServer({ ws : true });
proxy.on('error', function(err, req) {
  console.error(err, req.url);
});

function getHost(service, path) {
  var host = null;

  if(!service) {
    host = localServices[service.name];
    console.info('Using default url for service %s: %s', service.name, host);
    return host;
  }

  var userProvidedService = config.getUserProvidedSerice(service.name);
  return userProvidedService.host;
}

function getServiceName(requestUrl) {
  return _.find(serviceMapping, function(service){
    return requestUrl.match(service.path);
  });
}

function getTargetHost(req, res) {
  var path = req.url;
  var service = getServiceName(path);
  if(!service) {
    throw404(res, util.format("No service found for the path: %s", JSON.stringify(path)));
    return;
  }

  var host = getHost(service, path);
  if(!host) {
    throw404(res, util.format("No route found for service  %s", JSON.stringify(service)));
    return;
  }

  return host;
}

function forwardRequest(req, res) {
  var service = getServiceName(req.url)
  if (service.auth === 'basic') {
    req.headers['Authorization'] = 'basic ' + basicAuthCredentials;
  }
  if (service.auth === 'token') {
    if(req.user && req.user.accessToken) {
      req.headers['Authorization'] = 'bearer ' + req.user.accessToken;
    }
  }

  if(req.user && req.user.user_id) {
    req.headers['X-Seahorse-UserId'] = req.user.user_id;
    req.headers['X-Seahorse-UserName'] = req.user.user_name;
  } else {
    req.removeHeader('X-Seahorse-UserId');
    req.removeHeader('X-Seahorse-UserName');
  }

  req.headers['x-forwarded-host'] = req.headers['host'];
  req.clearTimeout();

  proxy.web(req, res, {
    target: getTargetHost(req, res),
    timeout: config.get('timeout')
  });
}

function forwardWebSocket(req, socket, head) {
  proxy.ws(req, socket, head, {
    target: getTargetHost(req, socket),
    timeout: 0
  });
}

function handleProxyError(res, serviceName, path) {
  return function(httpError) {
    if(!httpError) {
      return;
    }

    var error = gatewayErrors.getError(httpError.code);
    httpException.throw(res, error.code, error.title, util.format(error.description, serviceName, path), httpError);
  };
}

function throw404(res, message) {
  httpException.throw(res, 404, "Not Found", message);
}

module.exports = {
  forwardWebSocket: forwardWebSocket,
  forward: forwardRequest
};
