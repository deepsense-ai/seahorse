/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
/*global inject*/
'use strict';


describe('EntitiesAPIClient', () => {
  var module,
      EntitiesAPIClient;

  beforeEach(() => {
    module = angular.module('test', []);
    require('../BaseAPIClient.factory.js').inject(module);
    require('../EntitiesAPIClient.factory.js').inject(module);

    angular.mock.module('test');
    inject((_EntitiesAPIClient_) => {
      EntitiesAPIClient = _EntitiesAPIClient_;
    });
  });


  it('should be defined', () => {
    expect(EntitiesAPIClient).toBeDefined();
    expect(EntitiesAPIClient).toEqual(jasmine.any(Object));
  });


  describe('should have getReport method', () => {
    var $httpBackend,
        mockRequest;

    var id = 'test-01',
        url = '/api/entities/' + id + '/raport',
        response = {'test': true};

    beforeEach(() => {
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
      expect(EntitiesAPIClient.getReport).toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      $httpBackend.expectGET(url);

      let promise = EntitiesAPIClient.getReport(id);
      expect(promise).toEqual(jasmine.any(Object));
      expect(promise.then).toEqual(jasmine.any(Function));
      expect(promise.catch).toEqual(jasmine.any(Function));

      $httpBackend.flush();
    });

    it('which return promise & resolve it on request success', () => {
      let success = false,
          error   = false,
          responseData;

      $httpBackend.expectGET(url);

      let promise = EntitiesAPIClient.getReport(id);
      promise.then((data) => {
        success = true;
        responseData = data;
      }).catch(() => {
        error = true;
      });

      $httpBackend.flush();

      expect(success).toBe(true);
      expect(error).toBe(false);
      expect(responseData).toEqual(response);
    });

    it('which return promise & rejects it on request error', () => {
      let success = false,
          error   = false;

      $httpBackend.expectGET(url);

      let promise = EntitiesAPIClient.getReport(id);
      promise.then(() => {
        success = true;
      }).catch(() => {
        error = true;
      });

      mockRequest.respond(500, 'Server Error');
      $httpBackend.flush();

      expect(success).toBe(false);
      expect(error).toBe(true);
    });
  });

});
