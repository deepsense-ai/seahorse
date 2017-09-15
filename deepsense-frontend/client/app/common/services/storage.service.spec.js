'use strict';

describe('StorageService', () => {
  let StorageService;
  let mockRoot = 'root';
  let mockInvalidRoot = 'root2';
  let mockKey = 'key';
  let mockInvalidKey = 'key2';
  let mockValue = 'value';
  let mockObject = {};
  let mockSerializedObject;
  mockObject[mockKey] = mockValue;
  mockSerializedObject = JSON.stringify(mockObject);

  beforeEach(() => {
    let testModule = angular.module('test', []);
    require('./storage.service.js').inject(testModule);
    angular.mock.module('test');
    angular.mock.inject((_StorageService_) => {
      StorageService = _StorageService_;
    });
    window.localStorage.clear();
  });

  describe ('getRoot()', () => {
    it('should return root', () => {
      window.localStorage.setItem(mockRoot, mockSerializedObject);
      expect(StorageService.getRoot(mockRoot)).toEqual(mockObject);
    });


    it('should return empty object if invalid root is provided', () => {
      window.localStorage.setItem(mockRoot, mockSerializedObject);
      expect(StorageService.getRoot(mockInvalidRoot)).toEqual({});
    });
  });

  describe ('set()', () => {
    it('should set item to localStorage', () => {
      StorageService.set(mockRoot, mockKey, mockValue);
      expect(JSON.parse(window.localStorage.getItem(mockRoot))[mockKey]).toEqual(mockValue);
    });
  });

  describe ('get()', () => {
    it('should get item from localStorage', () => {
      window.localStorage.setItem(mockRoot, mockSerializedObject);
      expect(StorageService.get(mockRoot, mockKey)).toEqual(mockValue);
    });

    it('should return undefined if key is invalid', () => {
      window.localStorage.setItem(mockRoot, mockSerializedObject);
      expect(StorageService.get(mockRoot, mockInvalidKey)).toBeUndefined();
    });

    it('should return undefined if root is invalid', () => {
      window.localStorage.setItem(mockRoot, mockSerializedObject);
      expect(StorageService.get(mockInvalidRoot, mockKey)).toBeUndefined();
    });
  });

  describe ('remove()', () => {
    it('should remove element', () => {
      window.localStorage.setItem(mockRoot, mockSerializedObject);
      StorageService.remove(mockRoot, mockKey);
      expect(JSON.parse(window.localStorage.getItem(mockRoot))[mockKey]).toBeUndefined();
    });
  });
});
