/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
'use strict';

let resultIsPromise = require('./utils/result-is-promise.js');
let promiseIsResolved = require('./utils/promise-is-resolved.js');
let promiseIsRejected = require('./utils/promise-is-rejected.js');

const API_TYPE = 'batch';
const API_VERSION = '0.9.0';

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
        'apiPort': '',
        'apiVersion': API_VERSION
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

  describe('should have getWorkflow method', () => {
    let $httpBackend;
    let mockRequest;

    let id = 'workflow-id';
    let url = '/api/workflows/' + id;
    let response = {
      metadata: {
        type: API_TYPE,
        apiVersion: API_VERSION
      },
      workflow: {},
      thirdPartyData: {
        gui: {
          name: 'name',
          description: 'description'
        }
      }
    };

    beforeEach(() => {
      angular.mock.inject(($injector) => {
        $httpBackend = $injector.get('$httpBackend');
        mockRequest = $httpBackend.
          when('GET', url).
          respond(response);
        });
    });

    afterEach(function() {
      $httpBackend.verifyNoOutstandingExpectation();
      $httpBackend.verifyNoOutstandingRequest();
    });

    it('which is valid function', () => {
      expect(WorkflowsApiClient.getWorkflow).toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      resultIsPromise(
        $httpBackend,
        () => WorkflowsApiClient.getWorkflow(id),
        () => { $httpBackend.expectGET(url); }
      );
    });

    it('which return promise & resolve it on request success', () => {
      promiseIsResolved($httpBackend, url, response,
        () => WorkflowsApiClient.getWorkflow(id),
        () => { $httpBackend.expectGET(url); }
      );
    });

    it('which return promise & rejects it on request error', () => {
      promiseIsRejected($httpBackend, mockRequest,
        () => WorkflowsApiClient.getWorkflow(id),
        () => { $httpBackend.expectGET(url); }
      );
    });
  });

  describe('should have createWorkflow method', () => {
    let $httpBackend;
    let mockRequest;
    let url = '/api/workflows';
    let params = {
      'name': 'Draft',
      'description': 'Draft workflow'
    };
    let dataRequest = {
      metadata: {
        type: API_TYPE,
        apiVersion: API_VERSION
      },
      workflow: {
        nodes: [],
        connections: []
      },
      thirdPartyData: {
        gui: {
          name: params.name,
          description: params.description
        }
      }
    };
    let dataResponse = _.assign({}, dataRequest, {
      id: 'workflow-id'
    });

    beforeEach(() => {
      angular.mock.inject(($injector) => {
        $httpBackend = $injector.get('$httpBackend');
        mockRequest = $httpBackend.
          when('POST', url).
          respond(dataResponse);
      });
    });

    afterEach(function() {
      $httpBackend.verifyNoOutstandingExpectation();
      $httpBackend.verifyNoOutstandingRequest();
    });

    it('which is valid function', () => {
      expect(WorkflowsApiClient.createWorkflow).toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      resultIsPromise(
        $httpBackend,
        () => WorkflowsApiClient.createWorkflow(params),
        () => { $httpBackend.expectPOST(url, dataRequest); }
      );
    });

    it('which return promise & resolve it on request success', () => {
      promiseIsResolved(
        $httpBackend, url, dataResponse,
        () => WorkflowsApiClient.createWorkflow(params),
        () => { $httpBackend.expectPOST(url, dataRequest); }
      );
    });

    it('which return promise & rejects it on request error', () => {
      promiseIsRejected($httpBackend, mockRequest,
        () => WorkflowsApiClient.createWorkflow(params),
        () => { $httpBackend.expectPOST(url, dataRequest); }
      );
    });
  });

  describe('should have updateWorkflow method', () => {
    let $httpBackend;
    let mockRequest;

    let id = 'workflow-id';
    let url = `/api/workflows/${id}`;
    let serializedWorkflow = {
      id: id,
      workflow: {
        'nodes': [],
        'connections': []
      },
      thirdPartyData: {}
    };
    let dataRequest = {
      metadata: {
        type: API_TYPE,
        apiVersion: API_VERSION
      },
      workflow: serializedWorkflow.workflow,
      thirdPartyData: serializedWorkflow.thirdPartyData
    };
    let dataResponse = dataRequest;

    beforeEach(() => {
      angular.mock.inject(($injector) => {
        $httpBackend = $injector.get('$httpBackend');
        mockRequest = $httpBackend.
          when('PUT', url).
          respond(dataResponse);
        });
    });

    afterEach(function() {
      $httpBackend.verifyNoOutstandingExpectation();
      $httpBackend.verifyNoOutstandingRequest();
    });

    it('which is valid function', () => {
      expect(WorkflowsApiClient.updateWorkflow).toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      resultIsPromise(
        $httpBackend,
        () => WorkflowsApiClient.updateWorkflow(serializedWorkflow),
        () => { $httpBackend.expectPUT(url, dataRequest); }
      );
    });

    it('which return promise & resolve it on request success', () => {
      promiseIsResolved($httpBackend, url, dataResponse,
        () => WorkflowsApiClient.updateWorkflow(serializedWorkflow),
        () => { $httpBackend.expectPUT(url, dataRequest); }
      );
    });

    it('which return promise & rejects it on request error', () => {
      promiseIsRejected($httpBackend, mockRequest,
        () => WorkflowsApiClient.updateWorkflow(serializedWorkflow),
        () => { $httpBackend.expectPUT(url, dataRequest); }
      );
    });
  });
});
