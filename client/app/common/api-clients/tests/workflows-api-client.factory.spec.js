'use strict';

let resultIsPromise = require('./utils/result-is-promise.js');
let promiseIsResolved = require('./utils/promise-is-resolved.js');
let promiseIsRejected = require('./utils/promise-is-rejected.js');

const API_TYPE = 'batch';
const API_VERSION = '0.9.0';
const URL_API_VERSION = 'v1';

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
        'apiVersion': API_VERSION,
        'urlApiVersion': URL_API_VERSION
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
    let url = `/${URL_API_VERSION}/workflows/${id}`;
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

    afterEach(() => {
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
    let url = `/${URL_API_VERSION}/workflows`;
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
    let url = `/${URL_API_VERSION}/workflows/${id}`;
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

    afterEach(() => {
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

  describe('should have getLatestReport method', () => {
    let $httpBackend;
    let mockRequest;

    let id = 'workflow-id';
    let url = `/${URL_API_VERSION}/workflows/${id}/report`;
    let dataResponse = {
      metadata: {
        type: API_TYPE,
        apiVersion: API_VERSION
      },
      id: id,
      workflow: {
        'nodes': [],
        'connections': []
      },
      executionReport: {},
      thirdPartyData: {}
    };

    beforeEach(() => {
      angular.mock.inject(($injector) => {
        $httpBackend = $injector.get('$httpBackend');
        mockRequest = $httpBackend.
          when('GET', url).
          respond(dataResponse);
      });
    });

    afterEach(function() {
      $httpBackend.verifyNoOutstandingExpectation();
      $httpBackend.verifyNoOutstandingRequest();
    });

    it('which is valid function', () => {
      expect(WorkflowsApiClient.getLatestReport).toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      resultIsPromise(
        $httpBackend,
        () => WorkflowsApiClient.getLatestReport(id),
        () => { $httpBackend.expectGET(url); }
      );
    });

    it('which return promise & resolve it on request success', () => {
      promiseIsResolved($httpBackend, url, dataResponse,
        () => WorkflowsApiClient.getLatestReport(id),
        () => { $httpBackend.expectGET(url); }
      );
    });

    it('which return promise & rejects it on request error', () => {
      promiseIsRejected($httpBackend, mockRequest,
        () => WorkflowsApiClient.getLatestReport(id),
        () => { $httpBackend.expectGET(url); }
      );
    });
  });

  describe('should have getReport method', () => {
    let $httpBackend;
    let mockRequest;

    let id = 'report-id';
    let url = `/${URL_API_VERSION}/reports/${id}`;
    let dataResponse = {
      metadata: {
        type: API_TYPE,
        apiVersion: API_VERSION
      },
      id: id,
      workflow: {
        'nodes': [],
        'connections': []
      },
      executionReport: {},
      thirdPartyData: {}
    };

    beforeEach(() => {
      angular.mock.inject(($injector) => {
        $httpBackend = $injector.get('$httpBackend');
        mockRequest = $httpBackend.
          when('GET', url).
          respond(dataResponse);
      });
    });

    afterEach(() => {
      $httpBackend.verifyNoOutstandingExpectation();
      $httpBackend.verifyNoOutstandingRequest();
    });

    it('which is valid function', () => {
      expect(WorkflowsApiClient.getReport).toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      resultIsPromise(
        $httpBackend,
        () => WorkflowsApiClient.getReport(id),
        () => { $httpBackend.expectGET(url); }
      );
    });

    it('which return promise & resolve it on request success', () => {
      promiseIsResolved($httpBackend, url, dataResponse,
        () => WorkflowsApiClient.getReport(id),
        () => { $httpBackend.expectGET(url); }
      );
    });

    it('which return promise & rejects it on request error', () => {
      promiseIsRejected($httpBackend, mockRequest,
        () => WorkflowsApiClient.getReport(id),
        () => { $httpBackend.expectGET(url); }
      );
    });
  });

  describe('should have getResultsUploadTime method', () => {
    let $httpBackend;
    let mockRequest;

    let workflowId = 'workflow-id';
    let url = `/${URL_API_VERSION}/workflows/${workflowId}/results-upload-time`;
    let dataResponse = 'date';

    beforeEach(() => {
      angular.mock.inject(($injector) => {
        $httpBackend = $injector.get('$httpBackend');
        mockRequest = $httpBackend.
          when('GET', url).
          respond(dataResponse);
      });
    });

    afterEach(() => {
      $httpBackend.verifyNoOutstandingExpectation();
      $httpBackend.verifyNoOutstandingRequest();
    });

    it('which is valid function', () => {
      expect(WorkflowsApiClient.getResultsUploadTime).toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      resultIsPromise(
        $httpBackend,
        () => WorkflowsApiClient.getResultsUploadTime(workflowId),
        () => { $httpBackend.expectGET(url); }
      );
    });

    it('which return promise & resolve it on request success', () => {
      promiseIsResolved($httpBackend, url, dataResponse,
        () => WorkflowsApiClient.getResultsUploadTime(workflowId),
        () => { $httpBackend.expectGET(url); }
      );
    });

    it('which return promise & rejects it on request error', () => {
      promiseIsRejected($httpBackend, mockRequest,
        () => WorkflowsApiClient.getResultsUploadTime(workflowId),
        () => { $httpBackend.expectGET(url); }
      );
    });
  });
});
