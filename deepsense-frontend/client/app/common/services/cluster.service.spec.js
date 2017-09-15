'use strict';

describe('ClusterService', () => {
  let ClusterService;
  let PresetService;
  let StorageMock;

  const DEFAULT_PRESET = {
    'id': '1_Local',
    'name': 'default',
    'clusterType': 'standalone',
    'uri': 'spark://something',
    'executorMemory': 7,
    'executorCores': 5,
    'totalExecutorCores': 5,
    'numExecutors': 2,
    'params': '--this that',
    'isEditable': false,
    'isDefault': true
  };

  const MOCK_PRESET = {
    'id': '2_Local',
    'name': 'default',
    'clusterType': 'standalone',
    'uri': 'spark://something',
    'executorMemory': 7,
    'executorCores': 5,
    'totalExecutorCores': 5,
    'numExecutors': 2,
    'params': '--this that',
    'isEditable': false,
    'isDefault': false
  };

  const MOCK_WORKFLOW_ID = '8888-88888-8888888-8888888-8888888';
  const INVALID_WORKFLOW_ID = '8888-88888-8888888-8888888-8888889';
  const MAPPING_ROOT = 'workflow-preset';
  const MOCK_LOCALSTORAGE = {};
  MOCK_LOCALSTORAGE[MAPPING_ROOT] = {};
  MOCK_LOCALSTORAGE[MAPPING_ROOT][MOCK_WORKFLOW_ID] = MOCK_PRESET.id;

  beforeEach(() => {
    const testModule = angular.module('clusterTest', []);
    require('./cluster.service.js').inject(testModule);
    angular.mock.module('clusterTest');
    angular.mock.module(($provide) => {
      $provide.service('StorageService', function() {
        let mockData = {};
        this.set = function (root, key, value) {
          if (!mockData[root]) {
            mockData[root] = {};
          }
          mockData[root][key] = value;
        };
        this.get = function (root, key) {
          const temp = mockData[root] || {};

          return temp[key];
        };
        this.getRoot = function (root) {
          return mockData[root] || {};
        };
        this.remove = function (root, key) {
          const temp = mockData[root] || {};
          delete temp[key];
        };
        this.mock = function(newData) {
          mockData = JSON.parse(JSON.stringify(newData));
        };
        this.clear = function () {
          mockData = {};
        };
      });
      $provide.service('PresetService', function() {
        const mockMap = {
          '2_Local' : MOCK_PRESET
        };
        this.getPresetById = function (id) {
          return mockMap[id];
        };
        this.getDefaultPreset = function () {
          return DEFAULT_PRESET;
        };
      });
    });
    angular.mock.inject((_ClusterService_, _StorageService_, _PresetService_) => {
      ClusterService = _ClusterService_;
      PresetService = _PresetService_;
      StorageMock = _StorageService_;
    });
    StorageMock.clear();
  });

  describe ('getPresetByWorkflowId()', () => {
    it('should return preset if valid workflowId is provided', () => {
      StorageMock.mock(MOCK_LOCALSTORAGE);
      expect(ClusterService.getPresetByWorkflowId(MOCK_WORKFLOW_ID)).toEqual(MOCK_PRESET);
    });

    it('should return undefined if invalid workflowId is provided', () => {
      StorageMock.mock(MOCK_LOCALSTORAGE);
      expect(ClusterService.getPresetByWorkflowId(INVALID_WORKFLOW_ID)).toEqual(DEFAULT_PRESET);

    });
  });

  describe ('getPresetConfigObject()', () => {
    it('should return preset configuration for API if valid workflowId is provided', () => {
      const expected = {
        workflowId: MOCK_WORKFLOW_ID,
        cluster: MOCK_PRESET
      };
      StorageMock.mock(MOCK_LOCALSTORAGE);
      expect(ClusterService.getPresetConfigObject(MOCK_WORKFLOW_ID)).toEqual(expected);
    });

    it('should return default values if invalid workflowId is provided', () => {
      const expected = {
        workflowId: INVALID_WORKFLOW_ID,
        cluster: DEFAULT_PRESET
      };
      StorageMock.mock(MOCK_LOCALSTORAGE);
      expect(ClusterService.getPresetConfigObject(INVALID_WORKFLOW_ID)).toEqual(expected);

    });
  });

  describe ('bindPresetToWorkflow()', () => {
    it('should bind preset to workflowId and store it in storageService', () => {
      ClusterService.bindPresetToWorkflow(MOCK_WORKFLOW_ID, MOCK_PRESET.ID);
      expect(StorageMock.get(MAPPING_ROOT, MOCK_WORKFLOW_ID)).toEqual(MOCK_PRESET.ID);
    });
  });

});
