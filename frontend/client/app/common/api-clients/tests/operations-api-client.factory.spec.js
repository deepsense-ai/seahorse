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

'use strict';

describe('OperationsApiClient', () => {
  let globalInternal = {
    promiseIsReturned: ($httpBackend, url, functionReturningPromise) => {
      $httpBackend.expectGET(url);

      let promise = functionReturningPromise();
      expect(promise)
        .toEqual(jasmine.any(Object));
      expect(promise.then)
        .toEqual(jasmine.any(Function));
      expect(promise.catch)
        .toEqual(jasmine.any(Function));

      $httpBackend.flush();
    },
    promiseIsResolved: ($httpBackend, url, functionReturningPromise, expectedResponse) => {
      let success = false;
      let error = false;
      let responseData = null;

      $httpBackend.expectGET(url);

      functionReturningPromise()
        .then((data) => {
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
        .toEqual(expectedResponse);
    },
    promiseIsRejected: ($httpBackend, url, functionReturningPromise, mockRequest) => {
      let success = false;
      let error = false;

      $httpBackend.expectGET(url);

      functionReturningPromise()
        .then(() => {
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
    }
  };

  let testModule;
  let OperationsApiClient;

  const API_VERSION = '0.9.0';
  const URL_API_VERSION = 'v1';

  beforeEach(() => {
    testModule = angular.module('test', []);

    require('./../base-api-client.factory.js')
      .inject(testModule);
    require('./../operations-api-client.factory.js')
      .inject(testModule);

    angular.mock.module('test');
    angular.mock.module(($provide) => {
      $provide.constant('config', {
        'apiHost': '',
        'apiPort': '',
        'apiVersion': API_VERSION,
        'urlApiVersion': URL_API_VERSION
      });
    });

    angular.mock.inject((_OperationsApiClient_) => {
      OperationsApiClient = _OperationsApiClient_;
    });
  });

  it('should be defined', () => {
    expect(OperationsApiClient)
      .toBeDefined();
    expect(OperationsApiClient)
      .toEqual(jasmine.any(Object));
  });

  describe('should have getAll method', () => {
    let $httpBackend;
    let mockRequest;
    let url = `/${URL_API_VERSION}/operations`;
    let response = {
      'test': true
    };

    beforeEach(() => {
      angular.mock.inject(($injector) => {
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
      expect(OperationsApiClient.getAll)
        .toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      globalInternal.promiseIsReturned($httpBackend, url, () => OperationsApiClient.getAll());
    });

    it('which return promise & resolve it on request success', () => {
      globalInternal.promiseIsResolved($httpBackend, url, () => OperationsApiClient.getAll(), response);
    });

    it('which return promise & rejects it on request error', () => {
      globalInternal.promiseIsRejected($httpBackend, url, () => OperationsApiClient.getAll(), mockRequest);
    });
  });

  describe('should have get method', () => {
    let $httpBackend;
    let mockRequest;
    let id = 'id-01';
    let url = `/${URL_API_VERSION}/operations/${id}`;
    let response = {
      'test': true
    };

    beforeEach(() => {
      angular.mock.inject(($injector) => {
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
      expect(OperationsApiClient.get)
        .toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      globalInternal.promiseIsReturned($httpBackend, url, () => OperationsApiClient.get(id));
    });

    it('which return promise & resolve it on request success', () => {
      globalInternal.promiseIsResolved($httpBackend, url, () => OperationsApiClient.get(id), response);
    });

    it('which return promise & rejects it on request error', () => {
      globalInternal.promiseIsRejected($httpBackend, url, () => OperationsApiClient.get(id), mockRequest);
    });
  });

  describe('should have getCatalog method', () => {
    let $httpBackend;
    let mockRequest;
    let url = `/${URL_API_VERSION}/operations/catalog`;
    let response = {
      'test': true
    };

    beforeEach(() => {
      angular.mock.inject(($injector) => {
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
      expect(OperationsApiClient.getCatalog)
        .toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      globalInternal.promiseIsReturned($httpBackend, url, () => OperationsApiClient.getCatalog());
    });

    it('which return promise & resolve it on request success', () => {
      globalInternal.promiseIsResolved($httpBackend, url, () => OperationsApiClient.getCatalog(), response);
    });

    it('which return promise & rejects it on request error', () => {
      globalInternal.promiseIsRejected($httpBackend, url, () => OperationsApiClient.getCatalog(), mockRequest);
    });
  });

  describe('should have getHierarchy method', () => {
    let $httpBackend;
    let mockRequest;
    let url = `/${URL_API_VERSION}/operations/hierarchy`;
    let response = {
      'test': true
    };

    beforeEach(() => {
      angular.mock.inject(($injector) => {
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
      expect(OperationsApiClient.getHierarchy)
        .toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      globalInternal.promiseIsReturned($httpBackend, url, () => OperationsApiClient.getHierarchy());
    });

    it('which return promise & resolve it on request success', () => {
      globalInternal.promiseIsResolved($httpBackend, url, () => OperationsApiClient.getHierarchy(), response);
    });

    it('which return promise & rejects it on request error', () => {
      globalInternal.promiseIsRejected($httpBackend, url, () => OperationsApiClient.getHierarchy(), mockRequest);
    });
  });
});
