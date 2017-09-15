/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by: Piotr Zarówny
 */
/*global inject*/
'use strict';


describe('OperationsAPIClient', () => {
  let globalInternal = {
    promiseIsReturned: ($httpBackend, url, functionReturningPromise) => {
      $httpBackend.expectGET(url);

      let promise = functionReturningPromise();
      expect(promise).toEqual(jasmine.any(Object));
      expect(promise.then).toEqual(jasmine.any(Function));
      expect(promise.catch).toEqual(jasmine.any(Function));

      $httpBackend.flush();
    },
    promiseIsResolved: ($httpBackend, url, functionReturningPromise, expectedResponse) => {
      let success = false;
      let error   = false;
      let responseData = null;

      $httpBackend.expectGET(url);

      functionReturningPromise().
        then((data) => {
          success = true;
          responseData = data;
        }).
        catch(() => { error = true; });

      $httpBackend.flush();

      expect(success).toBe(true);
      expect(error).toBe(false);
      expect(responseData).toEqual(expectedResponse);
    },
    promiseIsRejected: ($httpBackend, url, functionReturningPromise, mockRequest) => {
      let success = false;
      let error   = false;

      $httpBackend.expectGET(url);

      functionReturningPromise().
        then(() => { success = true; }).
        catch(() => { error = true; });

      mockRequest.respond(500, 'Server Error');
      $httpBackend.flush();

      expect(success).toBe(false);
      expect(error).toBe(true);
    }
  };

  let testModule;
  let OperationsAPIClient;

  beforeEach(() => {
    testModule = angular.module('test', []);
    require('../BaseAPIClient.factory.js').inject(testModule);
    require('../OperationsAPIClient.factory.js').inject(testModule);

    angular.mock.module('test');
    inject((_OperationsAPIClient_) => {
      OperationsAPIClient = _OperationsAPIClient_;
    });
  });


  it('should be defined', () => {
    expect(OperationsAPIClient).toBeDefined();
    expect(OperationsAPIClient).toEqual(jasmine.any(Object));
  });


  describe('should have getAll method', () => {
    let $httpBackend;
    let mockRequest;
    let url = '/api/operations';
    let response = { 'test': true };

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
      expect(OperationsAPIClient.getAll).toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      globalInternal.promiseIsReturned($httpBackend, url, () => OperationsAPIClient.getAll());
    });

    it('which return promise & resolve it on request success', () => {
      globalInternal.promiseIsResolved($httpBackend, url, () => OperationsAPIClient.getAll(), response);
    });

    it('which return promise & rejects it on request error', () => {
      globalInternal.promiseIsRejected($httpBackend, url, () => OperationsAPIClient.getAll(), mockRequest);
    });
  });


  describe('should have get method', () => {
    let $httpBackend;
    let mockRequest;
    let id = 'id-01';
    let url = '/api/operations/' + id;
    let response = { 'test': true };

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
      expect(OperationsAPIClient.get).toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      globalInternal.promiseIsReturned($httpBackend, url, () => OperationsAPIClient.get(id));
    });

    it('which return promise & resolve it on request success', () => {
      globalInternal.promiseIsResolved($httpBackend, url, () => OperationsAPIClient.get(id), response);
    });

    it('which return promise & rejects it on request error', () => {
      globalInternal.promiseIsRejected($httpBackend, url, () => OperationsAPIClient.get(id), mockRequest);
    });
  });

  describe('should have getCatalog method', () => {
    let $httpBackend;
    let mockRequest;
    let url = '/api/operations/catalog';
    let response = { 'test': true };

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
      expect(OperationsAPIClient.getCatalog).toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      globalInternal.promiseIsReturned($httpBackend, url, () => OperationsAPIClient.getCatalog());
    });

    it('which return promise & resolve it on request success', () => {
      globalInternal.promiseIsResolved($httpBackend, url, () => OperationsAPIClient.getCatalog(), response);
    });

    it('which return promise & rejects it on request error', () => {
      globalInternal.promiseIsRejected($httpBackend, url, () => OperationsAPIClient.getCatalog(), mockRequest);
    });
  });

  describe('should have getHierarchy method', () => {
    let $httpBackend;
    let mockRequest;
    let url = '/api/operations/hierarchy';
    let response = { 'test': true };

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
      expect(OperationsAPIClient.getHierarchy).toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      globalInternal.promiseIsReturned($httpBackend, url, () => OperationsAPIClient.getHierarchy());
    });

    it('which return promise & resolve it on request success', () => {
      globalInternal.promiseIsResolved($httpBackend, url, () => OperationsAPIClient.getHierarchy(), response);
    });

    it('which return promise & rejects it on request error', () => {
      globalInternal.promiseIsRejected($httpBackend, url, () => OperationsAPIClient.getHierarchy(), mockRequest);
    });
  });
});
