/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
'use strict';


module.exports = function apiProxy(config) {
  var express = require('express'),
      httpProxy = require('http-proxy'),
      apiApp = express(),
      proxy = httpProxy.createProxyServer({
        target: config.url
      });


  /**
   * Handles proxy errors.
   */
  proxy.on('error', (error, request, response) => {
    response.writeHead(502, {
      'Content-Type': 'text/plain; charset=UTF-8'
    });
    console.error('error:', '502', request.method, request.url, error);
    response.end('502 - proxy source error');
  });

  /**
   * Handles response data processing.
   */
  proxy.on('proxyRes', (proxyResponse, request, response) => {
    let responseWrite = response.write,
        responseWriteHead = response.writeHead,
        responseEnd = response.end,
        responseData = '',
        doResponse = (data, status, contetType) => {
          responseWriteHead.call(response, status, {
            'Content-Type': contetType || 'application/json; charset=UTF-8',
            'Content-Length': Buffer.byteLength(data, 'utf8')
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
        console.error('error:', proxyResponse.statusCode, request.method, request.url, responseData);
        doResponse('502 - proxy source error', 502, 'text/plain; charset=UTF-8');
      } else {
        if (request.customHandler) {
          request.customHandler(JSON.parse(responseData), request).then((data) => {
            doResponse(JSON.stringify(data, null, 2), 200, 'application/json; charset=UTF-8');
          }).fail((error) => {
            console.error('error:', '500', request.method, request.url, error);
            doResponse('500 - proxy internal error', 500, 'text/plain; charset=UTF-8');
          });
        } else {
          doResponse(responseData, proxyResponse.statusCode, proxyResponse.headers['content-type']);
        }
      }
    };
  });

  /**
   * Handles proxy request.
   */
  proxy.on('proxyReq', (proxyRequest, request, response, options) => {
    if (request.method === 'PUT' || request.method === 'POST') {
      let requestBody = '';
      request.on('data', (source) => {
        requestBody += source;
      });
      request.on('end', (source) => {
        request.body = JSON.parse(requestBody.toString());
      });
    }

    // TODO: remove after full login implementation
    if (config.token) {
      proxyRequest.setHeader('X-Auth-Token', config.token);
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
      let params = {};
      if (resource.url) {
        params.target = resource.url;
      }
      proxy.web(request, response, params);
    } else {
      response.writeHead(404, {
        'Content-Type': 'text/plain; charset=UTF-8'
      });
      console.error('error:', '404', request.method, '/api' + request.url);
      response.end('404 - unknown resource');
    }
  });

  return apiApp;
};
