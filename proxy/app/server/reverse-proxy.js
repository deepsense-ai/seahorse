/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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
  if(_.isUndefined(service)) {
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
