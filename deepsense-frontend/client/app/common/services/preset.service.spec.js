'use strict';

describe('PresetService', () => {
  let PresetService;
  let StorageMock;

  const DEFAULT_PRESET = {
    'id': '1_local',
    'name': 'default',
    'clusterType': 'local',
    'uri': '',
    'userIP': '',
    'executorMemory': '2',
    'totalExecutorCores': 2,
    'executorCores': 2,
    'numExecutors': 2,
    'params': '--num-executors 2 --verbose',
    'isEditable': false,
    'isDefault': true
  };
  const MOCK_PRESET = {
    'id': '2_local',
    'name': 'Default',
    'clusterType': 'mesos',
    'uri': 'http://www.path/to/standalone',
    'userIP': '',
    'executorMemory': '2',
    'executorCores': 5,
    'totalExecutorCores': 5,
    'numExecutors': 2,
    'params': '--this that',
    'isEditable': false,
    'isDefault': false
  };
  const MOCK_PRESET_EDITABLE = {
    'id': '2_local',
    'name': 'Default',
    'clusterType': 'mesos',
    'uri': 'http://www.path/to/standalone',
    'userIP': '',
    'executorMemory': '2',
    'executorCores': 5,
    'totalExecutorCores': 5,
    'numExecutors': 2,
    'params': '--this that',
    'isEditable': true,
    'isDefault': false
  };
  const MOCK_PRESET_EDITABLE_CHANGED = {
    'id': '2_local',
    'name': 'Default',
    'clusterType': 'mesos',
    'uri': 'http://www.path/to/standalone',
    'userIP': '',
    'executorMemory': '3',
    'executorCores': 5,
    'totalExecutorCores': 5,
    'numExecutors': 2,
    'params': '--this that',
    'isEditable': true,
    'isDefault': false
  };
  const INVALID_MOCK_PRESET = {
    'id': '2_local',
    'name': 'Default',
    'clusterType': 'invalid-type',
    'uri': 'http://www.path/to/standalone',
    'userIP': '',
    'executorMemory': '2',
    'executorCores': 5,
    'totalExecutorCores': 5,
    'numExecutors': 2,
    'params': '--this that',
    'isEditable': false,
    'isDefault': false
  };
  const DEFAULT_STORE = {
    '1_local' : DEFAULT_PRESET
  };
  const MOCK_STORE = {
    '1_local' : DEFAULT_PRESET,
    '2_local' : MOCK_PRESET
  };
  const MOCK_LOCALSTORAGE = {
    'presets': MOCK_STORE
  };
  const MOCK_STORE_EDITABLE = {
    '1_local' : DEFAULT_PRESET,
    '2_local' : MOCK_PRESET_EDITABLE
  };
  const MOCK_LOCALSTORAGE_EDITABLE = {
    'presets': MOCK_STORE_EDITABLE
  };
  const INVALID_NAME = 'invalid-name';

  beforeEach(() => {
    const testModule = angular.module('presetTest', []);
    require('./preset.service.js').inject(testModule);
    angular.mock.module('presetTest');
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
    });
    angular.mock.inject((_PresetService_, _StorageService_) => {
      PresetService = _PresetService_;
      StorageMock = _StorageService_;
    });
    StorageMock.clear();
  });

  describe ('getAll()', () => {
    it('should by default return only default preset', () => {
      expect(PresetService.getAll()).toEqual(DEFAULT_STORE);
    });


    it('return proper presets', () => {
      StorageMock.mock(MOCK_LOCALSTORAGE);

      expect(PresetService.getAll()).toEqual(MOCK_STORE);
    });
  });

  describe ('getPresetById()', () => {
    it('should return preset if already in the store', () => {
      expect(PresetService.getPresetById('1_local')).toEqual(DEFAULT_PRESET);
    });

    it('should return undefined if not in the store', () => {
      expect(PresetService.getPresetById('2_local')).toBeUndefined();
    });
  });

  describe ('createPreset()', () => {
    it('should create preset if valid object is provided', () => {
      PresetService.createPreset(MOCK_PRESET);
      expect(Object.keys(StorageMock.getRoot('presets')).length).toEqual(1);
    });

    it('shouldn\'t create preset if invalid object is provided', () => {
      PresetService.createPreset(INVALID_MOCK_PRESET);
      expect(Object.keys(StorageMock.getRoot('presets')).length).toEqual(0);
    });
  });

  describe ('deletePreset()', () => {
    it('shouldn\'t delete default preset', () => {
      StorageMock.mock(MOCK_LOCALSTORAGE);
      expect(PresetService.deletePreset(DEFAULT_PRESET.id)).toEqual(false);
      expect(Object.keys(StorageMock.getRoot('presets')).length).toEqual(2);
    });

    it('should delete non-default, editable preset', () => {
      StorageMock.mock(MOCK_LOCALSTORAGE_EDITABLE);
      expect(PresetService.deletePreset(MOCK_PRESET_EDITABLE.id)).toEqual(true);
      expect(Object.keys(StorageMock.getRoot('presets')).length).toEqual(1);
    });

    it('shouldn\'t delete non-editable, non-default preset', () => {
      StorageMock.mock(MOCK_LOCALSTORAGE);
      expect(PresetService.deletePreset(MOCK_PRESET.id)).toEqual(false);
      expect(Object.keys(StorageMock.getRoot('presets')).length).toEqual(2);
    });
  });

  describe ('updatePreset()', () => {
    it('shouldn\'t update default preset', () => {
      StorageMock.mock(MOCK_LOCALSTORAGE);
      expect(PresetService.updatePreset(DEFAULT_PRESET)).toEqual(false);
    });

    it('should update non-default, editable preset', () => {
      StorageMock.mock(MOCK_LOCALSTORAGE_EDITABLE);
      expect(PresetService.updatePreset(MOCK_PRESET_EDITABLE_CHANGED)).toEqual(true);
      expect(StorageMock.get('presets', MOCK_PRESET_EDITABLE_CHANGED.id)).toEqual(MOCK_PRESET_EDITABLE_CHANGED);
    });

    it('shouldn\'t update when provided invalid preset', () => {
      StorageMock.mock(MOCK_LOCALSTORAGE_EDITABLE);
      expect(PresetService.updatePreset(INVALID_MOCK_PRESET)).toEqual(false);
      expect(StorageMock.get('presets', INVALID_MOCK_PRESET.id)).toEqual(MOCK_PRESET_EDITABLE);
    });
  });

  describe ('savePreset()', () => {
    it('shouldn\'t update default preset', () => {
      StorageMock.mock(MOCK_LOCALSTORAGE);
      expect(PresetService.savePreset(DEFAULT_PRESET)).toEqual(false);
    });

    it('should update non-default, editable preset', () => {
      StorageMock.mock(MOCK_LOCALSTORAGE_EDITABLE);
      expect(PresetService.savePreset(MOCK_PRESET_EDITABLE_CHANGED)).toEqual(true);
      expect(StorageMock.get('presets', MOCK_PRESET_EDITABLE_CHANGED.id)).toEqual(MOCK_PRESET_EDITABLE_CHANGED);
    });

    it('shouldn\'t update when provided invalid preset', () => {
      StorageMock.mock(MOCK_LOCALSTORAGE_EDITABLE);
      expect(PresetService.savePreset(INVALID_MOCK_PRESET)).toEqual(false);
      expect(StorageMock.get('presets', INVALID_MOCK_PRESET.id)).toEqual(MOCK_PRESET_EDITABLE);
    });

    it('should create preset if valid object is provided', () => {
      PresetService.savePreset(MOCK_PRESET);
      expect(Object.keys(StorageMock.getRoot('presets')).length).toEqual(1);
    });

    it('shouldn\'t create preset if invalid object is provided', () => {
      PresetService.savePreset(INVALID_MOCK_PRESET);
      expect(Object.keys(StorageMock.getRoot('presets')).length).toEqual(0);
    });
  });

  describe ('isValid()', () => {
    it('shouldn\'t validate valid preset', () => {
      expect(PresetService.isValid(INVALID_MOCK_PRESET)).toEqual(false);
    });

    it('should validate invalid preset', () => {
      expect(PresetService.isValid(MOCK_PRESET)).toEqual(true);
    });
  });

  describe ('getErrors()', () => {
    it('should return an object with errors if validator returned false', () => {
      PresetService.isValid(INVALID_MOCK_PRESET);
      expect(Object.keys(PresetService.getErrors()).length).toBeGreaterThan(0);
    });

    it('shouldn\'t return errors without validation call', () => {
      expect(Object.keys(PresetService.getErrors()).length).toBe(0);
    });
  });

  describe ('isNameUsed()', () => {
    it('should return true if default name is provided', () => {
      StorageMock.mock(MOCK_LOCALSTORAGE);
      expect(PresetService.isNameUsed(DEFAULT_PRESET.name)).toEqual(true);
    });
    it('should return true if existing name is provided', () => {
      StorageMock.mock(MOCK_LOCALSTORAGE);
      expect(PresetService.isNameUsed(MOCK_PRESET.name)).toEqual(true);
    });

    it('should return false when non-existing name is provided', () => {
      StorageMock.mock(MOCK_LOCALSTORAGE);
      expect(PresetService.isNameUsed(INVALID_NAME)).toEqual(false);
    });
  });

  describe ('getDefaultPreset()', () => {
    it('should return default preset', () => {
      expect(PresetService.getDefaultPreset()).toEqual(DEFAULT_PRESET);
    });
  });
});
