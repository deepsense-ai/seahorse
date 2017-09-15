/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
/*global console*/
'use strict';


module.exports = function apiProxy(config) {
  var bodyParser = require('body-parser'),
      express = require('express'),
      httpProxy = require('http-proxy'),
      apiApp = express(),
      proxy = httpProxy.createProxyServer({
        target: config.url
      });


  /**
   * Handles errors.
   */
  proxy.on('error', (error, request, response) => {
    response.writeHead(502, {'Content-Type':'text/plain'});
    response.end('502');
  });

  /**
   * Handles response data processing.
   */
  proxy.on('proxyRes', (proxyResponse, request, response) => {
    if (request.customHandler) {
      let responseWrite = response.write,
          responseWriteHead = response.writeHead,
          responseEnd = response.end;

      response.write = (source) => {
        if (proxyResponse.statusCode >= 400) {
          responseWriteHead.call(response, proxyResponse.statusCode, {'Content-Type':'text/plain'});
          responseWrite.call(response, source);
          responseEnd.call(response);
          return;
        } else {
          let data = JSON.parse(source);
          request.customHandler(data, request).then((data) => {
            responseWriteHead.call(response, 200);
            responseWrite.call(response, JSON.stringify(data, true, 2));
            responseEnd.call(response);
          }).fail((error) => {
            console.error(error);
            responseWriteHead.call(response, 500, {'Content-Type':'text/plain'});
            responseWrite.call(response, '500');
            responseEnd.call(response);
          });
        }
      };
      response.writeHead = () => {};
      response.end = () => {};
    }
  });


  var urlPathParts,
      resource;

  /**
   * Handles & proxies known requests.
   */
  apiApp.use(bodyParser.json());
  apiApp.all('*', (request, response) => {
    urlPathParts = request.url.split('/');
    resource = urlPathParts[1] ? config.resources[urlPathParts[1]] : null;

    if (resource) {
      request.customHandler = resource.handler;
      request.resourcePath = urlPathParts.slice(2).join('/');
      request.url = '/' + config.version + request.url;
      proxy.web(request, response);
    } else {
      response.writeHead(404, {'Content-Type':'text/plain'});
      response.end('404 - unknown resource');
    }
  });

  return apiApp;
};
