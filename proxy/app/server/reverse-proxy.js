/**
 * Copyright (c) 2016, CodiLime Inc.
 */
const request = require('request'),
    _ = require('underscore'),
    url = require('url'),
    util = require('util'),

    serviceMapping = require('./config/service-mapping'),
    config = require('./config/config'),
    httpException = require('./utils/http-exception'),
    gatewayErrors = require('./gateway-errors');

const basicAuthCredentials = new Buffer(
      config.get('WM_AUTH_USER') + ':' + config.get('WM_AUTH_PASS')
    ).toString('base64');

const httpProxy = require('http-proxy');
const proxy = httpProxy.createProxyServer({ ws : true });
proxy.on('error', function(err, req) {
  console.error(err, req.url);
});

function getTargetHost(req, res) {
  const path = req.url;

  const service = serviceMapping.getServiceForRequest(path);
  if(!service) {
    throw404(res, util.format("No service found for the path: %s", JSON.stringify(path)));
    return;
  }
  return service.host
}

function forward(req, res) {
  const service = serviceMapping.getServiceForRequest(req.url);
  if (service.auth === 'basic') {
    req.headers['authorization'] = 'basic ' + basicAuthCredentials;
  }
  if (service.auth === 'token') {
    if(req.user && req.user.accessToken) {
      req.headers['authorization'] = 'bearer ' + req.user.accessToken;
    }
  }

  if(req.user && req.user.user_id) {
    req.headers['x-seahorse-userid'] = req.user.user_id;
    req.headers['x-seahorse-username'] = req.user.user_name;
  } else {
    delete req.headers['x-seahorse-userid'];
    delete req.headers['x-seahorse-username'];
  }

  req.headers['x-forwarded-host'] = req.headers['host'];
  req.clearTimeout();

  const options = {
    target: getTargetHost(req, res),
    timeout: config.get('timeout'),
  };

  if(!_.isUndefined(service.proxyTimeout)) {
      options.proxyTimeout = service.proxyTimeout;
  }

  proxy.web(req, res, options, function (e) {
        console.error(e);
        if (!_.isUndefined(service.timeoutRedirectionPage)) {
          const waitPage = url.format({protocol: req.protocol, host: req.get("host"), pathname: service.timeoutRedirectionPage});
          res.writeHead(302, {'Location': waitPage});
          res.end();
        }
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

    const error = gatewayErrors.getError(httpError.code);
    httpException.throw(res, error.code, error.title, util.format(error.description, serviceName, path), httpError);
  };
}

function throw404(res, message) {
  httpException.throw(res, 404, "Not Found", message);
}

module.exports = {
  forwardWebSocket,
  forward
};
