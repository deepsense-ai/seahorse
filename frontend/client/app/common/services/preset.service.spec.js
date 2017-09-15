/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

describe('PresetService', () => {
  let PresetService;
  let WorkflowService;
  let PresetsApiService;
  let $rootScope;

  const DEFAULT_PRESET = {
    'id': 1,
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
    'id': 2,
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
    'id': 2,
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
    'id': 2,
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
    'id': 2,
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
  const MOCK_STORE = {
    1: DEFAULT_PRESET,
    2: MOCK_PRESET
  };
  const MOCK_STORE_EDITABLE = {
    1: DEFAULT_PRESET,
    2: MOCK_PRESET_EDITABLE
  };
  const INVALID_NAME = 'invalid-name';

  beforeEach(() => {
    const testModule = angular.module('presetTest', []);
    require('./preset.service.js').inject(testModule);
    angular.mock.module('presetTest');
    angular.mock.module(($provide) => {
      $provide.factory('WorkflowService', function() {
        return {
          fetchCluster: function() {},
          getRootWorkflow: function () {}
        };
      });
      $provide.service('PresetsApiService', function($q) {
        let mockData = {};
        this.get = function (key) {
          return mockData[key];
        };
        this.getAll = function () {
          return $q.when(mockData);
        };
        this.create = function (obj) {
          mockData[obj.id] = obj;
          return $q.when(true);
        };
        this.update = function (key, value) {
          mockData[key] = value;
          return $q.when(true);
        };
        this.remove = function (key) {
          delete mockData[key];
          return $q.when(true);
        };
        this.mock = function(newData) {
          mockData = JSON.parse(JSON.stringify(newData));
        };
        this.clear = function () {
          mockData = {};
        };
        this.mockData = function () {
          return mockData;
        };
      });
    });
    angular.mock.inject((_PresetService_, _PresetsApiService_, _$rootScope_, _WorkflowService_) => {
      PresetService = _PresetService_;
      PresetsApiService = _PresetsApiService_;
      WorkflowService = _WorkflowService_;
      $rootScope = _$rootScope_;
    });
    PresetsApiService.clear();
  });

  describe('getAll()', () => {
    it('should by default return only default preset', () => {
      expect(PresetService.getAll()).toBeUndefined();
    });
  });

  describe('createPreset()', () => {
    it('should create preset if valid object is provided', () => {
      PresetService.createPreset(MOCK_PRESET);
      expect(Object.keys(PresetsApiService.mockData()).length).toEqual(1);
    });
  });

  describe('deletePreset()', () => {
    it('should delete non-default, editable preset', () => {
      PresetsApiService.mock(MOCK_STORE_EDITABLE);
      PresetService.deletePreset(MOCK_PRESET_EDITABLE.id);
      expect(Object.keys(PresetsApiService.mockData()).length).toEqual(1);
    });
  });

  describe('updatePreset()', () => {
    it('should update non-default, editable preset', () => {
      PresetsApiService.mock(MOCK_STORE_EDITABLE);
      PresetService.updatePreset(MOCK_PRESET_EDITABLE_CHANGED);
      expect(PresetsApiService.get(MOCK_PRESET_EDITABLE_CHANGED.id)).toEqual(MOCK_PRESET_EDITABLE_CHANGED);
    });
  });

  describe('savePreset()', () => {
    it('should update non-default, editable preset', () => {
      PresetsApiService.mock(MOCK_STORE_EDITABLE);
      PresetService.savePreset(MOCK_PRESET_EDITABLE_CHANGED);
      expect(PresetsApiService.get(MOCK_PRESET_EDITABLE_CHANGED.id)).toEqual(MOCK_PRESET_EDITABLE_CHANGED);
    });

    it('should create preset if valid object is provided', () => {
      PresetService.savePreset(MOCK_PRESET);
      expect(Object.keys(PresetsApiService.mockData()).length).toEqual(1);
    });
  });

  describe('isValid()', () => {
    it('shouldn\'t validate valid preset', () => {
      expect(PresetService.isValid(INVALID_MOCK_PRESET)).toEqual(false);
    });

    it('should validate invalid preset', () => {
      expect(PresetService.isValid(MOCK_PRESET)).toEqual(true);
    });
  });

  describe('getErrors()', () => {
    it('should return an object with errors if validator returned false', () => {
      PresetService.isValid(INVALID_MOCK_PRESET);
      expect(Object.keys(PresetService.getErrors()).length).toBeGreaterThan(0);
    });

    it('shouldn\'t return errors without validation call', () => {
      expect(Object.keys(PresetService.getErrors()).length).toBe(0);
    });
  });

  describe('isNameUsed()', () => {
    it('should return true if existing name is provided', () => {
      PresetsApiService.mock(MOCK_STORE);
      PresetService.fetch();
      $rootScope.$digest();
      expect(PresetService.isNameUsed(MOCK_PRESET.name)).toEqual(true);
    });

    it('should return false when non-existing name is provided', () => {
      PresetsApiService.mock(MOCK_STORE);
      PresetService.fetch();
      $rootScope.$digest();
      expect(PresetService.isNameUsed(INVALID_NAME)).toEqual(false);
    });
  });
});
