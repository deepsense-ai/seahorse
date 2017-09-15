/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
'use strict';

let resultIsPromise = require('./utils/result-is-promise.js');
let promiseIsResolved = require('./utils/promise-is-resolved.js');
let promiseIsRejected = require('./utils/promise-is-rejected.js');

describe('ExperimentApiClient', () => {
  let module;
  let ExperimentApiClient;

  beforeEach(() => {
    module = angular.module('test', []);

    require('./../base-api-client.factory.js').inject(module);
    require('./../experiment-api-client.factory.js').inject(module);

    angular.mock.module('test');
    angular.mock.module(($provide) => {
      $provide.constant('config', {
        'apiHost': '',
        'apiPort': ''
      });
    });

    angular.mock.inject((_ExperimentApiClient_) => {
      ExperimentApiClient = _ExperimentApiClient_;
    });
  });

  it('should be defined', () => {
    expect(ExperimentApiClient).toBeDefined();
    expect(ExperimentApiClient).toEqual(jasmine.any(Object));
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
      expect(ExperimentApiClient.getList).toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      resultIsPromise(
        $httpBackend,
        () => ExperimentApiClient.getList(),
        () => { $httpBackend.expectGET(url); }
      );
    });

    it('which return promise & resolve it on request success', () => {
      promiseIsResolved($httpBackend, url, response,
        () => ExperimentApiClient.getList(),
        () => { $httpBackend.expectGET(url); }
      );
    });

    it('which return promise & rejects it on request error', () => {
      promiseIsRejected($httpBackend, mockRequest,
        () => ExperimentApiClient.getList(),
        () => { $httpBackend.expectGET(url); }
      );
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
      expect(ExperimentApiClient.getData).toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      resultIsPromise(
        $httpBackend,
        () => ExperimentApiClient.getData(id),
        () => { $httpBackend.expectGET(url); }
      );
    });

    it('which return promise & resolve it on request success', () => {
      promiseIsResolved($httpBackend, url, response,
        () => ExperimentApiClient.getData(id),
        () => { $httpBackend.expectGET(url); }
      );
    });

    it('which return promise & rejects it on request error', () => {
      promiseIsRejected($httpBackend, mockRequest,
        () => ExperimentApiClient.getData(id),
        () => { $httpBackend.expectGET(url); }
      );
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
      expect(ExperimentApiClient.saveData).toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      resultIsPromise(
        $httpBackend,
        () => ExperimentApiClient.saveData({'experiment': data}),
        () => { $httpBackend.expectPUT(url, {'experiment': data}); }
      );
    });

    it('which return promise & resolve it on request success', () => {
      promiseIsResolved($httpBackend, url, data,
        () => ExperimentApiClient.saveData({'experiment': data}),
        () => { $httpBackend.expectPUT(url, {'experiment': data}); }
      );
    });

    it('which return promise & rejects it on request error', () => {
      promiseIsRejected($httpBackend, mockRequest,
        () => ExperimentApiClient.saveData({'experiment': data}),
        () => { $httpBackend.expectPUT(url, {'experiment': data}); }
      );
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
      expect(ExperimentApiClient.runExperiment).toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      resultIsPromise(
        $httpBackend,
        () => ExperimentApiClient.runExperiment(id),
        () => { $httpBackend.expectPOST(url, {'launch': {}}); }
      );
    });

    it('which return promise & resolve it on request success', () => {
      promiseIsResolved($httpBackend, url, data,
        () => ExperimentApiClient.runExperiment(id),
        () => { $httpBackend.expectPOST(url, {'launch': {}}); }
      );
    });

    it('which return promise & rejects it on request error', () => {
      promiseIsRejected($httpBackend, mockRequest,
        () => ExperimentApiClient.runExperiment(id),
        () => { $httpBackend.expectPOST(url, {'launch': {}}); }
      );
    });

    it('which handles targetNodes params', () => {
      $httpBackend.expectPOST(url, {'launch': {'targetNodes': nodes}});

      let promise = ExperimentApiClient.runExperiment(id, {'targetNodes': nodes});
      expect(promise).toEqual(jasmine.any(Object));

      $httpBackend.flush();
    });
  });

  describe('should have abortExperiment method', () => {
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
      expect(ExperimentApiClient.abortExperiment).toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      resultIsPromise(
        $httpBackend,
        () => ExperimentApiClient.abortExperiment(id),
        () => { $httpBackend.expectPOST(url, {'abort': {}}); }
      );
    });

    it('which return promise & resolve it on request success', () => {
      promiseIsResolved($httpBackend, url, data,
        () => ExperimentApiClient.abortExperiment(id),
        () => { $httpBackend.expectPOST(url, {'abort': {}}); }
      );
    });

    it('which return promise & rejects it on request error', () => {
      promiseIsRejected($httpBackend, mockRequest,
        () => ExperimentApiClient.abortExperiment(id),
        () => { $httpBackend.expectPOST(url, {'abort': {}}); }
      );
    });

    it('which handles nodes params', () => {
      $httpBackend.expectPOST(url, {'abort': {'nodes': nodes}});

      let promise = ExperimentApiClient.abortExperiment(id, {'nodes': nodes});
      expect(promise).toEqual(jasmine.any(Object));

      $httpBackend.flush();
    });
  });

  describe('should have createExperiment method', () => {
    let $httpBackend;
    let mockRequest;
    let url = '/api/experiments';
    let params = {
      'name': 'Draft',
      'description': 'Draft experiment'
    };
    let dataRequest = {
      'experiment': {
        'name': params.name,
        'description': params.description,
        'graph': {
          'nodes': [],
          'edges': []
        }
      }
    };
    let dataResponse = _.assign({}, dataRequest, {
      id: 'experiment-id'
    });

    beforeEach(() => {
      angular.mock.inject(($injector) => {
        $httpBackend = $injector.get('$httpBackend');
        mockRequest = $httpBackend
          .when('POST', url)
          .respond(dataResponse);
      });
    });

    afterEach(function() {
      $httpBackend.verifyNoOutstandingExpectation();
      $httpBackend.verifyNoOutstandingRequest();
    });

    it('which is valid function', () => {
      expect(ExperimentApiClient.createExperiment).toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      resultIsPromise(
        $httpBackend,
        () => ExperimentApiClient.createExperiment(params),
        () => { $httpBackend.expectPOST(url, dataRequest); }
      );
    });

    it('which return promise & resolve it on request success', () => {
      promiseIsResolved(
        $httpBackend, url, dataResponse,
        () => ExperimentApiClient.createExperiment(params),
        () => { $httpBackend.expectPOST(url, dataRequest); }
      );
    });

    it('which return promise & rejects it on request error', () => {
      promiseIsRejected($httpBackend, mockRequest,
        () => ExperimentApiClient.createExperiment(params),
        () => { $httpBackend.expectPOST(url, dataRequest); }
      );
    });
  });

  describe('should have modifyExperiment method', () => {
    let $httpBackend;
    let mockRequest;
    let experimentId = 'experiment-id';
    let url = '/api/experiments/' + experimentId;
    let params = {
      'name': 'Draft',
      'description': 'Draft experiment',
      'graph': {
        'nodes': [],
        'edges': []
      }
    };
    let dataRequest = {
      'experiment': {
        'name': params.name,
        'description': params.description,
        'graph': {
          'nodes': [],
          'edges': []
        }
      }
    };
    let dataResponse = _.assign({}, dataRequest);

    beforeEach(() => {
      angular.mock.inject(($injector) => {
        $httpBackend = $injector.get('$httpBackend');
        mockRequest = $httpBackend
          .when('PUT', url)
          .respond(dataResponse);
      });
    });

    afterEach(function() {
      $httpBackend.verifyNoOutstandingExpectation();
      $httpBackend.verifyNoOutstandingRequest();
    });

    it('which is valid function', () => {
      expect(ExperimentApiClient.modifyExperiment).toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      resultIsPromise(
        $httpBackend,
        () => ExperimentApiClient.modifyExperiment(experimentId, params),
        () => { $httpBackend.expectPUT(url, dataRequest); }
      );
    });

    it('which return promise & resolve it on request success', () => {
      promiseIsResolved(
        $httpBackend, url, dataResponse,
        () => ExperimentApiClient.modifyExperiment(experimentId, params),
        () => { $httpBackend.expectPUT(url, dataRequest); }
      );
    });

    it('which return promise & rejects it on request error', () => {
      promiseIsRejected($httpBackend, mockRequest,
        () => ExperimentApiClient.modifyExperiment(experimentId, params),
        () => { $httpBackend.expectPUT(url, dataRequest); }
      );
    });
  });

  describe('should have deleteExperiment method', () => {
    let $httpBackend;
    let mockRequest;
    let experimentId = 'experiment-id';
    let url = '/api/experiments/' + experimentId;
    let dataResponse = 'OK';

    beforeEach(() => {
      angular.mock.inject(($injector) => {
        $httpBackend = $injector.get('$httpBackend');
        mockRequest = $httpBackend
          .when('DELETE', url)
          .respond(dataResponse);
      });
    });

    afterEach(function() {
      $httpBackend.verifyNoOutstandingExpectation();
      $httpBackend.verifyNoOutstandingRequest();
    });

    it('which is valid function', () => {
      expect(ExperimentApiClient.deleteExperiment).toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      resultIsPromise(
        $httpBackend,
        () => ExperimentApiClient.deleteExperiment(experimentId),
        () => { $httpBackend.expectDELETE(url); }
      );
    });

    it('which return promise & resolve it on request success', () => {
      promiseIsResolved(
        $httpBackend, url, dataResponse,
        () => ExperimentApiClient.deleteExperiment(experimentId),
        () => { $httpBackend.expectDELETE(url); }
      );
    });

    it('which return promise & rejects it on request error', () => {
      promiseIsRejected($httpBackend, mockRequest,
        () => ExperimentApiClient.deleteExperiment(experimentId),
        () => { $httpBackend.expectDELETE(url); }
      );
    });
  });
});
