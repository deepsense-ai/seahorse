/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by: Grzegorz Swatowski
 */
'use strict';

let resultIsPromise = require('./utils/result-is-promise.js');
let promiseIsResolved = require('./utils/promise-is-resolved.js');
let promiseIsRejected = require('./utils/promise-is-rejected.js');

describe('ModelApiClient', () => {
  let module;
  let ModelApiClient;

  beforeEach(() => {
    module = angular.module('test', []);

    require('./../base-api-client.factory.js').inject(module);
    require('./../model-api-client.factory.js').inject(module);

    angular.mock.module('test');
    angular.mock.module(($provide) => {
      $provide.constant('config', {
        'apiHost': '',
        'apiPort': ''
      });
    });

    angular.mock.inject((_ModelApiClient_) => {
      ModelApiClient = _ModelApiClient_;
    });
  });

  it('should be defined', () => {
    expect(ModelApiClient).toBeDefined();
    expect(ModelApiClient).toEqual(jasmine.any(Object));
  });

  describe('should have deployModel method', () => {
    let $httpBackend;
    let mockRequest;

    let id = 'sample-id';
    let url = `/api/models/${id}/deploy`;
    let response = { 'test': true };

    beforeEach(() => {
      angular.mock.inject(($injector) => {
        $httpBackend = $injector.get('$httpBackend');
        mockRequest = $httpBackend
          .when('POST', url)
          .respond(response);
      });
    });

    afterEach(function() {
      $httpBackend.verifyNoOutstandingExpectation();
      $httpBackend.verifyNoOutstandingRequest();
    });

    it('which is valid function', () => {
      expect(ModelApiClient.deployModel).toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      resultIsPromise(
        $httpBackend,
        () => ModelApiClient.deployModel(id),
        () => { $httpBackend.expectPOST(url); }
      );
    });

    it('which return promise & resolve it on request success', () => {
      promiseIsResolved($httpBackend, url, response,
        () => ModelApiClient.deployModel(id),
        () => { $httpBackend.expectPOST(url); }
      );
    });

    it('which return promise & rejects it on request error', () => {
      promiseIsRejected($httpBackend, mockRequest,
        () => ModelApiClient.deployModel(id),
        () => { $httpBackend.expectPOST(url); }
      );
    });
  });
});
