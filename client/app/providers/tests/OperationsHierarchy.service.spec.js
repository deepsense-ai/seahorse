/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by: Grzegorz Swatowski
 */
'use strict';

let getHierarchyResponseMock = {
  'traits': {
    'T0': {
      'name': 'T0',
      'parents': []
    },
    'T1': {
      'name': 'T1',
      'parents': [
        'T0'
      ]
    },
    'T2': {
      'name': 'T2',
      'parents': []
    },
    'T3': {
      'name': 'T3',
      'parents': [
        'T1',
        'T2'
      ]
    },
    'T7': {
      'name': 'T7',
      'parents': [
        'T3',
        'T4'
      ]
    }
  },
  'classes': {
    'T4': {
      'name': 'T4',
      'parent': null,
      'traits': [
        'T2'
      ]
    },
    'T5': {
      'name': 'T5',
      'parent': null,
      'traits': [
        'T2'
      ]
    },
    'T6': {
      'name': 'T6',
      'parent': 'T5',
      'traits': [
        'T1'
      ]
    }
  }
};


describe('OperationsHierarchy', () => {
  let testModule;
  let OperationsHierarchyService;

  beforeEach(() => {
    testModule = angular.module('test', []);
    require('../BaseAPIClient.factory.js').inject(testModule);
    require('../OperationsAPIClient.factory.js').inject(testModule);
    require('../OperationsHierarchy.service.js').inject(testModule);

    angular.mock.module('test');

    angular.mock.module(($provide) => {
      $provide.factory('OperationsAPIClient', ($q) => {
        var responseSuccess = true;

        var requestAPI = (response) => {
          let deferred = $q.defer();
          if (responseSuccess) {
            deferred.resolve(response);
          } else {
            deferred.reject();
          }
          return deferred.promise;
        };

        return {
          getHierarchy: () => requestAPI(getHierarchyResponseMock),
          changeResponseStatus: (state) => { responseSuccess = state; }
        };
      });
    });

    angular.mock.inject((_OperationsHierarchyService_) => {
      OperationsHierarchyService = _OperationsHierarchyService_;
    });
  });

  it('should be defined', () => {
    expect(OperationsHierarchyService).toBeDefined();
    expect(OperationsHierarchyService).toEqual(jasmine.any(Object));
  });

  describe('should have load method', () => {
    it('which is valid function', () => {
      expect(OperationsHierarchyService.load).toEqual(jasmine.any(Function));
    });

    it('which returns a promise object', () => {
      let promise = OperationsHierarchyService.load();
      expect(promise).toEqual(jasmine.any(Object));
      expect(promise.then).toEqual(jasmine.any(Function));
      expect(promise.catch).toEqual(jasmine.any(Function));
    });

    it(
      'which returns a promise object that should be resolved on successful response',
      angular.mock.inject(($rootScope, OperationsAPIClient) => {
        let success = false;
        let error   = false;

        OperationsAPIClient.changeResponseStatus(true);

        OperationsHierarchyService.load().
          then(() => { success = true; }).
          catch(() => { error = true; });
        $rootScope.$apply();

        expect(success).toBe(true);
        expect(error).toBe(false);
      })
    );

    it(
      'which returns a promise object that should be rejected on error response',
      angular.mock.inject(($rootScope, OperationsAPIClient) => {
        let success = false;
        let error   = false;

        OperationsAPIClient.changeResponseStatus(false);

        OperationsHierarchyService.load().
          then(() => { success = true; }).
          catch(() => { error = true; });
        $rootScope.$apply();

        expect(success).toBe(false);
        expect(error).toBe(true);
      })
    );

    it(
      'which should make only one request to the API.',
      angular.mock.inject(($q, $rootScope, OperationsAPIClient) => {
        let success = false;
        let error   = false;

        OperationsAPIClient.changeResponseStatus(true);

        spyOn(OperationsHierarchyService, 'load').and.callThrough();
        spyOn(OperationsAPIClient, 'getHierarchy').and.callThrough();


        OperationsHierarchyService.load().
          then(() => { success = true; }).
          catch(() => { error = true; });

        $rootScope.$apply();

        expect(OperationsHierarchyService.load).toHaveBeenCalled();
        expect(OperationsAPIClient.getHierarchy).toHaveBeenCalled();

        expect(success).toBe(true);
        expect(error).toBe(false);


        success = false;
        error = false;

        OperationsHierarchyService.load().
          then(() => { success = true; }).
          catch(() => { error = true; });

        $rootScope.$apply();

        expect(OperationsHierarchyService.load).toHaveBeenCalled();
        expect(OperationsHierarchyService.load.calls.count()).toBe(2);
        expect(OperationsAPIClient.getHierarchy.calls.count()).toBe(1);

        expect(success).toBe(true);
        expect(error).toBe(false);
      })
    );
  });

  describe('should have IsDescendantOf method', () => {
    it('which is valid function', () => {
      expect(OperationsHierarchyService.IsDescendantOf).toEqual(jasmine.any(Function));
    });

    describe('which returns proper response', () => {
      beforeEach(angular.mock.inject(($rootScope, OperationsAPIClient) => {
        OperationsAPIClient.changeResponseStatus(true);
        OperationsHierarchyService.load();
        $rootScope.$apply();
      }));

      describe('for a node that is not a descendant', () => {
        it('of one node', () => {
          expect(OperationsHierarchyService.IsDescendantOf('T0', ['T1'])).toBe(false);
          expect(OperationsHierarchyService.IsDescendantOf('T0', ['T2'])).toBe(false);
          expect(OperationsHierarchyService.IsDescendantOf('T0', ['T3'])).toBe(false);

          expect(OperationsHierarchyService.IsDescendantOf('T3', ['T4'])).toBe(false);
          expect(OperationsHierarchyService.IsDescendantOf('T3', ['T7'])).toBe(false);
          expect(OperationsHierarchyService.IsDescendantOf('T3', ['T5'])).toBe(false);

          expect(OperationsHierarchyService.IsDescendantOf('T4', ['T7'])).toBe(false);
          expect(OperationsHierarchyService.IsDescendantOf('T4', ['T3'])).toBe(false);
          expect(OperationsHierarchyService.IsDescendantOf('T4', ['T0'])).toBe(false);

          expect(OperationsHierarchyService.IsDescendantOf('T6', ['T7'])).toBe(false);
          expect(OperationsHierarchyService.IsDescendantOf('T6', ['T4'])).toBe(false);
          expect(OperationsHierarchyService.IsDescendantOf('T6', ['T3'])).toBe(false);
        });

        it('of more than one nodes', () => {
          expect(OperationsHierarchyService.IsDescendantOf('T0', ['T0', 'T1'])).toBe(false);

          expect(OperationsHierarchyService.IsDescendantOf('T1', ['T0', 'T1', 'T3'])).toBe(false);

          expect(OperationsHierarchyService.IsDescendantOf('T3', ['T0', 'T1', 'T2', 'T4'])).toBe(false);

          expect(OperationsHierarchyService.IsDescendantOf('T4', ['T2', 'T7'])).toBe(false);

          expect(OperationsHierarchyService.IsDescendantOf('T6', ['T5', 'T3'])).toBe(false);
          expect(OperationsHierarchyService.IsDescendantOf('T6', ['T0', 'T1', 'T5', 'T2', 'T3'])).toBe(false);

          expect(OperationsHierarchyService.IsDescendantOf('T7', ['T0', 'T1', 'T2', 'T3', 'T4', 'T5'])).toBe(false);
        });
      });

      describe('for a node that is a descendant', () => {
        it('of itself', () => {
          expect(OperationsHierarchyService.IsDescendantOf('T0', ['T0'])).toBe(true);
          expect(OperationsHierarchyService.IsDescendantOf('T2', ['T2'])).toBe(true);
          expect(OperationsHierarchyService.IsDescendantOf('T7', ['T7'])).toBe(true);
        });

        it('of one node', () => {
          expect(OperationsHierarchyService.IsDescendantOf('T1', ['T0'])).toBe(true);

          expect(OperationsHierarchyService.IsDescendantOf('T3', ['T1'])).toBe(true);
          expect(OperationsHierarchyService.IsDescendantOf('T3', ['T2'])).toBe(true);
          expect(OperationsHierarchyService.IsDescendantOf('T3', ['T0'])).toBe(true);

          expect(OperationsHierarchyService.IsDescendantOf('T4', ['T2'])).toBe(true);

          expect(OperationsHierarchyService.IsDescendantOf('T5', ['T2'])).toBe(true);

          expect(OperationsHierarchyService.IsDescendantOf('T6', ['T0'])).toBe(true);
          expect(OperationsHierarchyService.IsDescendantOf('T6', ['T1'])).toBe(true);
          expect(OperationsHierarchyService.IsDescendantOf('T6', ['T5'])).toBe(true);
          expect(OperationsHierarchyService.IsDescendantOf('T6', ['T2'])).toBe(true);

          expect(OperationsHierarchyService.IsDescendantOf('T7', ['T0'])).toBe(true);
          expect(OperationsHierarchyService.IsDescendantOf('T7', ['T1'])).toBe(true);
          expect(OperationsHierarchyService.IsDescendantOf('T7', ['T2'])).toBe(true);
          expect(OperationsHierarchyService.IsDescendantOf('T7', ['T3'])).toBe(true);
          expect(OperationsHierarchyService.IsDescendantOf('T7', ['T4'])).toBe(true);
        });

        it('of more than one nodes', () => {
          expect(OperationsHierarchyService.IsDescendantOf('T3', ['T0', 'T1', 'T2'])).toBe(true);
          expect(OperationsHierarchyService.IsDescendantOf('T6', ['T0', 'T1', 'T5', 'T2'])).toBe(true);
          expect(OperationsHierarchyService.IsDescendantOf('T7', ['T0', 'T1', 'T2', 'T3', 'T4'])).toBe(true);
        });
      });
    });
  });
});
