/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
'use strict';

var express = require('express'),
    httpProxy = require('http-proxy');

var config = {
  'url': 'http://localhost:8000/apimock',
  'version': 'V1',
  'resources': {
    'login': {
    },
    'entities': {
    },
    'experiments': {
    },
    'operations': {
    }
  }
};

var apiProxy = express(),
    proxy = httpProxy.createProxyServer({
      target: config.url
    });


/**
 * Handles response data processing.
 */
proxy.on('proxyRes', (proxyResponse, request, response) => {
  if (request.customHandler) {
    let responseWrite = response.write,
        responseEnd = response.end;

    response.write = (source) => {
        let data = JSON.parse(source);
        request.customHandler(data);
        responseWrite.call(response, JSON.stringify(data, true, 2));
        responseEnd.call(response);
    };
    response.end = () => {};
  }
});


var urlParts,
    resource;

/**
 * Handles & proxies known requests.
 */
apiProxy.all('*', (request, response) => {
  urlParts = request.url.split('/');
  resource = urlParts[1] ? config.resources[urlParts[1]] : null;

  if (resource) {
    request.customHandler = resource.handler;
    request.url = '/' + config.version + request.url;

    proxy.web(request, response);
  } else {
    response.writeHead(404, {'Content-Type':'text/plain'});
    response.end('404');
  }
});

module.exports = apiProxy;
