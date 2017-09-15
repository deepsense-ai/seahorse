'use strict';

let resultIsPromise = require('./utils/result-is-promise.js');
let promiseIsResolved = require('./utils/promise-is-resolved.js');
let promiseIsRejected = require('./utils/promise-is-rejected.js');

const API_TYPE = 'batch';
const API_VERSION = '0.6.0';
const API_HOST = 'https://editor.seahorse.deepsense.io';
const API_PORT = '443';
const URL_API_VERSION = 'v1';

describe('WorkflowsApiClient', () => {
  let module;
  let WorkflowsApiClient;
  let ServerCommunication;

  beforeEach(() => {
    module = angular.module('test', []);

    require('./../base-api-client.factory.js').inject(module);
    require('./../workflows-api-client.factory.js').inject(module);
    require('../../../server-communication/server-communication.js').inject(module);

    angular.mock.module('test');
    angular.mock.module(($provide) => {
      $provide.constant('config', {
        'apiHost': API_HOST,
        'docsHost': 'https://seahorse.deepsense.io',
        'notebookHost': 'http://localhost:8888',
        'apiPort': API_PORT,
        'apiVersion': API_VERSION,
        'editorVersion': '1.0.0',
        'urlApiVersion': URL_API_VERSION,
        'resultsRefreshInterval': 10000,
        'socketConnectionHost': 'http://127.0.0.1:15674/',
        'socketReconnectionInterval': 1000,
        'queueRoutes': {
          'connect': '/exchange/seahorse/to_executor',
          'executionStatus': '/exchange/<%= workflowId %>/to_editor',
          'launch/abort': '/exchange/<%= workflowId %>/to_executor'
        }
      });
    });

    angular.mock.inject((_WorkflowsApiClient_, _ServerCommunication_) => {
      WorkflowsApiClient = _WorkflowsApiClient_;
      ServerCommunication = _ServerCommunication_;
    });
  });

  it('should be defined', () => {
    expect(WorkflowsApiClient)
      .toBeDefined();
    expect(WorkflowsApiClient)
      .toEqual(jasmine.any(Object));
  });

  describe('should have getWorkflow method', () => {
    let $httpBackend;
    let mockRequest;

    let id = 'workflow-id';
    let url = `${API_HOST}:${API_PORT}/${URL_API_VERSION}/workflows/${id}`;
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
        mockRequest = $httpBackend.when('GET', url).respond(response);
      });
    });

    afterEach(() => {
      $httpBackend.verifyNoOutstandingExpectation();
      $httpBackend.verifyNoOutstandingRequest();
    });

    it('which is valid function', () => {
      expect(WorkflowsApiClient.getWorkflow)
        .toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      resultIsPromise(
        $httpBackend, () => WorkflowsApiClient.getWorkflow(id), () => {
          $httpBackend.expectGET(url);
        }
      );
    });

    it('which return promise & resolve it on request success', () => {
      promiseIsResolved($httpBackend, url, response, () => WorkflowsApiClient.getWorkflow(id), () => {
        $httpBackend.expectGET(url);
      });
    });

    it('which return promise & rejects it on request error', () => {
      promiseIsRejected($httpBackend, mockRequest, () => WorkflowsApiClient.getWorkflow(id), () => {
        $httpBackend.expectGET(url);
      });
    });
  });

  describe('should have createWorkflow method', () => {
    let $httpBackend;
    let mockRequest;
    let url = `${API_HOST}:${API_PORT}/${URL_API_VERSION}/workflows`;
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
        mockRequest = $httpBackend.when('POST', url).respond(dataResponse);
      });
    });

    afterEach(function () {
      $httpBackend.verifyNoOutstandingExpectation();
      $httpBackend.verifyNoOutstandingRequest();
    });

    it('which is valid function', () => {
      expect(WorkflowsApiClient.createWorkflow)
        .toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      resultIsPromise(
        $httpBackend, () => WorkflowsApiClient.createWorkflow(params), () => {
          $httpBackend.expectPOST(url, dataRequest);
        }
      );
    });

    it('which return promise & resolve it on request success', () => {
      promiseIsResolved(
        $httpBackend, url, dataResponse, () => WorkflowsApiClient.createWorkflow(params), () => {
          $httpBackend.expectPOST(url, dataRequest);
        }
      );
    });

    it('which return promise & rejects it on request error', () => {
      promiseIsRejected($httpBackend, mockRequest, () => WorkflowsApiClient.createWorkflow(params), () => {
        $httpBackend.expectPOST(url, dataRequest);
      });
    });
  });

  describe('should have updateWorkflow method', () => {
    let $httpBackend;
    let mockRequest;
    let config;

    let id = 'workflow-id';
    let url = `${API_HOST}:${API_PORT}/${URL_API_VERSION}/workflows/${id}`;
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
        config = $injector.get('config');
        $httpBackend = $injector.get('$httpBackend');
        mockRequest = $httpBackend.when('PUT', url).respond(dataResponse);
      });

      spyOn(WorkflowsApiClient, 'updateWorkflow').and.callThrough();
      spyOn(ServerCommunication, 'updateWorkflow');
    });

    afterEach(() => {
      $httpBackend.verifyNoOutstandingExpectation();
      $httpBackend.verifyNoOutstandingRequest();
    });

    it('which is valid function', () => {
      expect(WorkflowsApiClient.updateWorkflow)
        .toEqual(jasmine.any(Function));
    });

    it('has been called', () => {
      WorkflowsApiClient.updateWorkflow(serializedWorkflow);
      expect(WorkflowsApiClient.updateWorkflow).toHaveBeenCalled();
    });

    it('has been called with proper parameters', () => {
      WorkflowsApiClient.updateWorkflow(serializedWorkflow);
      expect(WorkflowsApiClient.updateWorkflow).toHaveBeenCalledWith(serializedWorkflow);
    });

    it('which call ServerCommunication.updateWorkflow with proper parameters', () => {
      WorkflowsApiClient.updateWorkflow(serializedWorkflow);
      expect(WorkflowsApiClient.updateWorkflow).toHaveBeenCalled();
      //expect(ServerCommunication.updateWorkflow).toHaveBeenCalled();
    });

    it('which call ServerCommunication.updateWorkflow with proper parameters', () => {
      WorkflowsApiClient.updateWorkflow(serializedWorkflow);
      expect(ServerCommunication.updateWorkflow).toHaveBeenCalledWith(dataRequest);
    });
  });

  describe('should have getLatestReport method', () => {
    let $httpBackend;
    let mockRequest;

    let id = 'workflow-id';
    let url = `${API_HOST}:${API_PORT}/${URL_API_VERSION}/workflows/${id}/report`;
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
        mockRequest = $httpBackend.when('GET', url).respond(dataResponse);
      });
    });

    afterEach(function () {
      $httpBackend.verifyNoOutstandingExpectation();
      $httpBackend.verifyNoOutstandingRequest();
    });

    it('which is valid function', () => {
      expect(WorkflowsApiClient.getLatestReport)
        .toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      resultIsPromise(
        $httpBackend, () => WorkflowsApiClient.getLatestReport(id), () => {
          $httpBackend.expectGET(url);
        }
      );
    });

    it('which return promise & resolve it on request success', () => {
      promiseIsResolved($httpBackend, url, dataResponse, () => WorkflowsApiClient.getLatestReport(id), () => {
        $httpBackend.expectGET(url);
      });
    });

    it('which return promise & rejects it on request error', () => {
      promiseIsRejected($httpBackend, mockRequest, () => WorkflowsApiClient.getLatestReport(id), () => {
        $httpBackend.expectGET(url);
      });
    });
  });

  describe('should have getReport method', () => {
    let $httpBackend;
    let mockRequest;

    let id = 'report-id';
    let url = `${API_HOST}:${API_PORT}/${URL_API_VERSION}/reports/${id}`;
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
        mockRequest = $httpBackend.when('GET', url).respond(dataResponse);
      });
    });

    afterEach(() => {
      $httpBackend.verifyNoOutstandingExpectation();
      $httpBackend.verifyNoOutstandingRequest();
    });

    it('which is valid function', () => {
      expect(WorkflowsApiClient.getReport)
        .toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      resultIsPromise(
        $httpBackend, () => WorkflowsApiClient.getReport(id), () => {
          $httpBackend.expectGET(url);
        }
      );
    });

    it('which return promise & resolve it on request success', () => {
      promiseIsResolved($httpBackend, url, dataResponse, () => WorkflowsApiClient.getReport(id), () => {
        $httpBackend.expectGET(url);
      });
    });

    it('which return promise & rejects it on request error', () => {
      promiseIsRejected($httpBackend, mockRequest, () => WorkflowsApiClient.getReport(id), () => {
        $httpBackend.expectGET(url);
      });
    });
  });

  describe('should have getResultsUploadTime method', () => {
    let $httpBackend;
    let mockRequest;

    let workflowId = 'workflow-id';
    let url = `${API_HOST}:${API_PORT}/${URL_API_VERSION}/workflows/${workflowId}/results-upload-time`;
    let dataResponse = 'date';

    beforeEach(() => {
      angular.mock.inject(($injector) => {
        $httpBackend = $injector.get('$httpBackend');
        mockRequest = $httpBackend.when('GET', url).respond(dataResponse);
      });
    });

    afterEach(() => {
      $httpBackend.verifyNoOutstandingExpectation();
      $httpBackend.verifyNoOutstandingRequest();
    });

    it('which is valid function', () => {
      expect(WorkflowsApiClient.getResultsUploadTime)
        .toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      resultIsPromise(
        $httpBackend, () => WorkflowsApiClient.getResultsUploadTime(workflowId), () => {
          $httpBackend.expectGET(url);
        }
      );
    });

    it('which return promise & resolve it on request success', () => {
      promiseIsResolved($httpBackend, url, dataResponse, () => WorkflowsApiClient.getResultsUploadTime(workflowId), () => {
        $httpBackend.expectGET(url);
      });
    });

    it('which return promise & rejects it on request error', () => {
      promiseIsRejected($httpBackend, mockRequest, () => WorkflowsApiClient.getResultsUploadTime(workflowId), () => {
        $httpBackend.expectGET(url);
      });
    });
  });
});
