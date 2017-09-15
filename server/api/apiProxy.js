/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
/*global console*/
'use strict';


module.exports = function apiProxy(config) {
  var express = require('express'),
      httpProxy = require('http-proxy'),
      apiApp = express(),
      proxy = httpProxy.createProxyServer({
        target: config.url
      });


  /**
   * Handles errors.
   */
  proxy.on('error', (error, request, response) => {
    response.writeHead(502, {
      'Content-Type': 'text/plain; charset=UTF-8'
    });
    response.end('502');
  });


  /**
   * Handles response data processing.
   */
  proxy.on('proxyRes', (proxyResponse, request, response) => {
    if (request.customHandler) {
      let responseWrite = response.write,
          responseWriteHead = response.writeHead,
          responseEnd = response.end,
          responseData = '',
          doResponse = (data, status, contetType) => {
            responseWriteHead.call(response, status, {
              'Content-Type': contetType + '; charset=UTF-8',
              'Content-Length': data.length
            });
            responseWrite.call(response, data);
            responseEnd.call(response);
          };

      proxyResponse.on('data', (source) => {
        responseData += source;
      });

      response.write = () => {};
      response.writeHead = () => {};
      response.end = () => {
        if (proxyResponse.statusCode >= 400) {
          doResponse(responseData, proxyResponse.statusCode, 'text/plain');
          return;
        } else {
          request.customHandler(JSON.parse(responseData), request).then((data) => {
            doResponse(JSON.stringify(data, null, 2), 200, 'application/json');
          }).fail((error) => {
            console.error(error);
            doResponse('500', 500, 'text/plain');
          });
        }
      };
    } else {
      response.header('Content-Type', 'application/json; charset=UTF-8');
    }
  });


  // TODO: remove after full login implementation
  proxy.on('proxyReq', (proxyRequest, request, response, options) => {
    if (config.token) {
      proxyRequest.setHeader('X-Auth-Token', config.token);
    }

    if (request.customHandler && (request.method === 'PUT' || request.method === 'POST')) {
      request.on('data', (source) => {
        request.body = JSON.parse(source.toString());
      });
    }
  });


  var urlPathParts,
      resource;

  /**
   * Handles & proxies known requests.
   */
  apiApp.all('*', (request, response) => {
    urlPathParts = request.url.split('/');
    resource = urlPathParts[1] ? config.resources[urlPathParts[1]] : null;

    if (resource) {
      request.customHandler = resource.handler;
      request.resourcePath = urlPathParts.slice(2).join('/');
      request.url = '/' + config.version + request.url;
      proxy.web(request, response);
    } else {
      response.writeHead(404, {
        'Content-Type': 'text/plain; charset=UTF-8'
      });
      response.end('404 - unknown resource');
    }
  });

  return apiApp;
};
