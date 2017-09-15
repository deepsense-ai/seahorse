/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
/*global inject*/
'use strict';


describe('BaseAPIClient', () => {
  var module,
      BaseAPIClient;

  beforeEach(() => {
    module = angular.module('test', []);
    require('../factory.BaseAPIClient.js').inject(module);

    angular.mock.module('test');
    inject((_BaseAPIClient_) => {
      BaseAPIClient = _BaseAPIClient_;
    });
  });


  it('should be defined', () => {
    expect(BaseAPIClient).toBeDefined();
    expect(BaseAPIClient).toEqual(jasmine.any(Function));
  });

  it('should have defined request methods', () => {
    let client = new BaseAPIClient();
    expect(client.METHOD_GET).toEqual(jasmine.any(String));
    expect(client.METHOD_POST).toEqual(jasmine.any(String));
    expect(client.METHOD_PUT).toEqual(jasmine.any(String));
    expect(client.METHOD_DELETE).toEqual(jasmine.any(String));
  });


  describe('should have request method', () => {
    var client,
        $httpBackend,
        mockRequest;

    var url = '/test/url',
        response = {'test': true};

    beforeEach(() => {
      client = new BaseAPIClient();
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
      expect(client.makeRequest).toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      $httpBackend.expectGET(url);

      let promise = client.makeRequest('GET', url);
      expect(promise).toEqual(jasmine.any(Object));
      expect(promise.then).toEqual(jasmine.any(Function));

      $httpBackend.flush();
    });

    it('which return promise & resolve on request success', () => {
      $httpBackend.expectGET(url);

      let promise = client.makeRequest('GET', url);
      $httpBackend.flush();

      promise.then((data) => {
        expect(data).toEqual(response);
      });
    });

  });

});
