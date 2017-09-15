/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
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

/* global inject */

'use strict';

describe('BaseApiClient', () => {
  var module;
  var BaseApiClient;

  beforeEach(() => {
    module = angular.module('test', []);

    require('./../base-api-client.factory.js')
      .inject(module);

    angular.mock.module('test');
    angular.mock.module(($provide) => {
      $provide.constant('config', {
        'apiHost': '',
        'apiPort': ''
      });
    });

    angular.mock.inject((_BaseApiClient_) => {
      BaseApiClient = _BaseApiClient_;
    });
  });

  it('should be defined', () => {
    expect(BaseApiClient)
      .toBeDefined();
    expect(BaseApiClient)
      .toEqual(jasmine.any(Function));
  });

  it('should have defined request methods', () => {
    let client = new BaseApiClient();
    expect(client.METHOD_GET)
      .toEqual(jasmine.any(String));
    expect(client.METHOD_POST)
      .toEqual(jasmine.any(String));
    expect(client.METHOD_PUT)
      .toEqual(jasmine.any(String));
    expect(client.METHOD_DELETE)
      .toEqual(jasmine.any(String));
  });

  describe('should have request method', () => {
    var client;
    var $httpBackend;
    var mockRequest;

    var url = '/test/url';
    var response = {
        'test': true
      };

    beforeEach(() => {
      client = new BaseApiClient();
      inject(($injector) => {
        $httpBackend = $injector.get('$httpBackend');
        mockRequest = $httpBackend
          .when('GET', url)
          .respond(response);
      });
    });

    afterEach(function() {
      $httpBackend.verifyNoOutstandingExpectation();
      $httpBackend.verifyNoOutstandingRequest();
    });

    it('which is valid function', () => {
      expect(client.makeRequest)
        .toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      $httpBackend.expectGET(url);

      let promise = client.makeRequest('GET', url);
      expect(promise)
        .toEqual(jasmine.any(Object));
      expect(promise.then)
        .toEqual(jasmine.any(Function));
      expect(promise.catch)
        .toEqual(jasmine.any(Function));

      $httpBackend.flush();
    });

    it('which return promise & resolve it on request success', () => {
      let success = false;
      let error = false;
      let responseData;

      $httpBackend.expectGET(url);

      let promise = client.makeRequest('GET', url);
      promise.then((data) => {
          success = true;
          responseData = data;
        })
        .catch(() => {
          error = true;
        });

      $httpBackend.flush();
      expect(success)
        .toBe(true);
      expect(error)
        .toBe(false);
      expect(responseData)
        .toEqual(response);
    });

    it('which return promise & rejects it on request error', () => {
      let success = false;
      let error = false;

      $httpBackend.expectGET(url);

      let promise = client.makeRequest('GET', url);
      promise.then(() => {
          success = true;
        })
        .catch(() => {
          error = true;
        });

      mockRequest.respond(500, 'Server Error');
      $httpBackend.flush();

      expect(success)
        .toBe(false);
      expect(error)
        .toBe(true);
    });

  });

});
