/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
'use strict';

let resultIsPromise = require('./utils/result-is-promise.js');
let promiseIsResolved = require('./utils/promise-is-resolved.js');
let promiseIsRejected = require('./utils/promise-is-rejected.js');

describe('WorkflowsApiClient', () => {
  let module;
  let WorkflowsApiClient;

  beforeEach(() => {
    module = angular.module('test', []);

    require('./../base-api-client.factory.js').inject(module);
    require('./../workflows-api-client.factory.js').inject(module);

    angular.mock.module('test');
    angular.mock.module(($provide) => {
      $provide.constant('config', {
        'apiHost': '',
        'apiPort': ''
      });
    });

    angular.mock.inject((_WorkflowsApiClient_) => {
      WorkflowsApiClient = _WorkflowsApiClient_;
    });
  });

  it('should be defined', () => {
    expect(WorkflowsApiClient).toBeDefined();
    expect(WorkflowsApiClient).toEqual(jasmine.any(Object));
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
      expect(WorkflowsApiClient.getData).toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      resultIsPromise(
        $httpBackend,
        () => WorkflowsApiClient.getData(id),
        () => { $httpBackend.expectGET(url); }
      );
    });

    it('which return promise & resolve it on request success', () => {
      promiseIsResolved($httpBackend, url, response,
        () => WorkflowsApiClient.getData(id),
        () => { $httpBackend.expectGET(url); }
      );
    });

    it('which return promise & rejects it on request error', () => {
      promiseIsRejected($httpBackend, mockRequest,
        () => WorkflowsApiClient.getData(id),
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
      expect(WorkflowsApiClient.saveData).toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      resultIsPromise(
        $httpBackend,
        () => WorkflowsApiClient.saveData({'experiment': data}),
        () => { $httpBackend.expectPUT(url, {'experiment': data}); }
      );
    });

    it('which return promise & resolve it on request success', () => {
      promiseIsResolved($httpBackend, url, data,
        () => WorkflowsApiClient.saveData({'experiment': data}),
        () => { $httpBackend.expectPUT(url, {'experiment': data}); }
      );
    });

    it('which return promise & rejects it on request error', () => {
      promiseIsRejected($httpBackend, mockRequest,
        () => WorkflowsApiClient.saveData({'experiment': data}),
        () => { $httpBackend.expectPUT(url, {'experiment': data}); }
      );
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
      expect(WorkflowsApiClient.createExperiment).toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      resultIsPromise(
        $httpBackend,
        () => WorkflowsApiClient.createExperiment(params),
        () => { $httpBackend.expectPOST(url, dataRequest); }
      );
    });

    it('which return promise & resolve it on request success', () => {
      promiseIsResolved(
        $httpBackend, url, dataResponse,
        () => WorkflowsApiClient.createExperiment(params),
        () => { $httpBackend.expectPOST(url, dataRequest); }
      );
    });

    it('which return promise & rejects it on request error', () => {
      promiseIsRejected($httpBackend, mockRequest,
        () => WorkflowsApiClient.createExperiment(params),
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
      expect(WorkflowsApiClient.modifyExperiment).toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      resultIsPromise(
        $httpBackend,
        () => WorkflowsApiClient.modifyExperiment(experimentId, params),
        () => { $httpBackend.expectPUT(url, dataRequest); }
      );
    });

    it('which return promise & resolve it on request success', () => {
      promiseIsResolved(
        $httpBackend, url, dataResponse,
        () => WorkflowsApiClient.modifyExperiment(experimentId, params),
        () => { $httpBackend.expectPUT(url, dataRequest); }
      );
    });

    it('which return promise & rejects it on request error', () => {
      promiseIsRejected($httpBackend, mockRequest,
        () => WorkflowsApiClient.modifyExperiment(experimentId, params),
        () => { $httpBackend.expectPUT(url, dataRequest); }
      );
    });
  });
});
