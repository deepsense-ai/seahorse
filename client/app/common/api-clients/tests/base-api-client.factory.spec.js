/*global inject*/

'use strict';

describe('BaseApiClient', () => {
  var module,
      BaseApiClient;

  beforeEach(() => {
    module = angular.module('test', []);

    require('./../base-api-client.factory.js').inject(module);

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
    expect(BaseApiClient).toBeDefined();
    expect(BaseApiClient).toEqual(jasmine.any(Function));
  });

  it('should have defined request methods', () => {
    let client = new BaseApiClient();
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
      expect(client.makeRequest).toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      $httpBackend.expectGET(url);

      let promise = client.makeRequest('GET', url);
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

      let promise = client.makeRequest('GET', url);
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

      let promise = client.makeRequest('GET', url);
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
