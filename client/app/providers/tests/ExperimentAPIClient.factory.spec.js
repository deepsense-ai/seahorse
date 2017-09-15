/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
'use strict';


describe('ExperimentAPIClient', () => {
  var module,
      ExperimentAPIClient;

  beforeEach(() => {
    module = angular.module('test', []);
    require('../BaseAPIClient.factory.js').inject(module);
    require('../ExperimentAPIClient.factory.js').inject(module);

    angular.mock.module('test');
    angular.mock.inject((_ExperimentAPIClient_) => {
      ExperimentAPIClient = _ExperimentAPIClient_;
    });
  });


  it('should be defined', () => {
    expect(ExperimentAPIClient).toBeDefined();
    expect(ExperimentAPIClient).toEqual(jasmine.any(Object));
  });


  describe('should have getList method', () => {
    var $httpBackend,
        mockRequest;

    var url = '/api/experiments',
        response = {'test': true};

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
      expect(ExperimentAPIClient.getList).toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      $httpBackend.expectGET(url);

      let promise = ExperimentAPIClient.getList();
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

      let promise = ExperimentAPIClient.getList();
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

      let promise = ExperimentAPIClient.getList();
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


  describe('should have getData method', () => {
    var $httpBackend,
        mockRequest;

    var id = 'experiemnt-id',
        url = '/api/experiments/' + id,
        response = {'test': true};

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
      expect(ExperimentAPIClient.getData).toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      $httpBackend.expectGET(url);

      let promise = ExperimentAPIClient.getData(id);
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

      let promise = ExperimentAPIClient.getData(id);
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

      let promise = ExperimentAPIClient.getData(id);
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


  describe('should have saveData method', () => {
    var $httpBackend,
        mockRequest;

    var id = 'experiemnt-id',
        url = '/api/experiments/' + id,
        data = {
          'id': id,
          'data': {
            'test': true
          }
        };

    beforeEach(() => {
      angular.mock.inject(($injector) => {
        $httpBackend = $injector.get('$httpBackend');
        mockRequest = $httpBackend
          .when('PUT', url)
          .respond(data);
        });
    });

    afterEach(function() {
      $httpBackend.verifyNoOutstandingExpectation();
      $httpBackend.verifyNoOutstandingRequest();
    });


    it('which is valid function', () => {
      expect(ExperimentAPIClient.saveData).toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      $httpBackend.expectPUT(url, {'experiment': data});

      let promise = ExperimentAPIClient.saveData({'experiment': data});
      expect(promise).toEqual(jasmine.any(Object));
      expect(promise.then).toEqual(jasmine.any(Function));
      expect(promise.catch).toEqual(jasmine.any(Function));

      $httpBackend.flush();
    });

    it('which return promise & resolve it on request success', () => {
      let success = false,
          error   = false,
          responseData;

      $httpBackend.expectPUT(url, {'experiment': data});

      let promise = ExperimentAPIClient.saveData({'experiment': data});
      promise.then((data) => {
        success = true;
        responseData = data;
      }).catch(() => {
        error = true;
      });

      $httpBackend.flush();

      expect(success).toBe(true);
      expect(error).toBe(false);
      expect(responseData).toEqual(data);
    });

    it('which return promise & rejects it on request error', () => {
      let success = false,
          error   = false;

      $httpBackend.expectPUT(url, {'experiment': data});

      let promise = ExperimentAPIClient.saveData({'experiment': data});
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

  describe('should have runExperiment method', () => {
    var $httpBackend,
        mockRequest;

    var id = 'experiemnt-id',
        url = '/api/experiments/' + id  + '/action',
        nodes = ['node-1', 'node-3'],
        data = {
          'id': id,
          'data': {
            'test': true
          }
        };

    beforeEach(() => {
      angular.mock.inject(($injector) => {
        $httpBackend = $injector.get('$httpBackend');
        mockRequest = $httpBackend
          .when('POST', url)
          .respond(data);
        });
    });

    afterEach(function() {
      $httpBackend.verifyNoOutstandingExpectation();
      $httpBackend.verifyNoOutstandingRequest();
    });


    it('which is valid function', () => {
      expect(ExperimentAPIClient.runExperiment).toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      $httpBackend.expectPOST(url, {'lunch': {}});

      let promise = ExperimentAPIClient.runExperiment(id);
      expect(promise).toEqual(jasmine.any(Object));
      expect(promise.then).toEqual(jasmine.any(Function));
      expect(promise.catch).toEqual(jasmine.any(Function));

      $httpBackend.flush();
    });

    it('which return promise & resolve it on request success', () => {
      let success = false,
          error   = false,
          responseData;

      $httpBackend.expectPOST(url, {'lunch': {}});

      let promise = ExperimentAPIClient.runExperiment(id);
      promise.then((data) => {
        success = true;
        responseData = data;
      }).catch(() => {
        error = true;
      });

      $httpBackend.flush();

      expect(success).toBe(true);
      expect(error).toBe(false);
      expect(responseData).toEqual(data);
    });

    it('which return promise & rejects it on request error', () => {
      let success = false,
          error   = false;

      $httpBackend.expectPOST(url, {'lunch': {}});

      let promise = ExperimentAPIClient.runExperiment(id);
      promise.then(() => {
        success = true;
      }).catch(() => {
        error = true;
      });

      mockRequest.respond(409, 'Conflict');
      $httpBackend.flush();

      expect(success).toBe(false);
      expect(error).toBe(true);
    });

    it('which handles targetNodes params', () => {
      $httpBackend.expectPOST(url, {'lunch': {'targetNodes': nodes}});

      let promise = ExperimentAPIClient.runExperiment(id, {'targetNodes': nodes});
      expect(promise).toEqual(jasmine.any(Object));

      $httpBackend.flush();
    });

  });

});
