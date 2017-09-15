/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
'use strict';


describe('Operations', () => {
  var module,
      Operations;


  var mockOperations = {
        'id-01': {
          'id': 'id-01',
          'category': 'c1',
          'value': 101
        },
        'id-02': {
          'id': 'id-02',
          'category': 'c2',
          'value': 102
        },
      },
      mockCatalog = {
        'catalog': [
          {
            'id': 'c1',
            'name': 'Category1',
            'items': [
              {
                'id': 'id-01',
              }
            ]
          },
          {
            'id': 'c1',
            'name': 'Category1',
            'items': [
              {
                'id': 'id-02',
              }
            ]
          }
        ]
      };

  beforeEach(() => {
    module = angular.module('test', []);
    require('../Operations.factory.js').inject(module);

    angular.mock.module('test');
    angular.mock.module(($provide) => {
      $provide.factory('OperationsAPIClient', ($q) => {
        var requestSuccess = true;

        var requestAPI = (response) => {
          let deferred = $q.defer();
          if (requestSuccess) {
            deferred.resolve(response);
          } else {
            deferred.reject();
          }
          return deferred.promise;
        };

        return {
          getAll: () => requestAPI({
            'operations': mockOperations
          }),
          getCatalog: () => requestAPI(mockCatalog),
          changeRequestState: (state) => {
            requestSuccess = state;
          }
        };
      });
    });
    angular.mock.inject((_Operations_) => {
      Operations = _Operations_;
    });
  });


  it('should be defined', () => {
    expect(Operations).toBeDefined();
    expect(Operations).toEqual(jasmine.any(Object));
  });

  it('should have defined methods', () => {
    expect(Operations.getAll).toEqual(jasmine.any(Function));
    expect(Operations.get).toEqual(jasmine.any(Function));
    expect(Operations.getCatalog).toEqual(jasmine.any(Function));
  });


  function testMethod(methodName, apiMethodName, expectedData, paramId) {
    describe(methodName + ' method should', () => {
      it('return promise', () => {
        let promise = Operations[methodName](paramId);
        expect(promise).toEqual(jasmine.any(Object));
        expect(promise.then).toEqual(jasmine.any(Function));
        expect(promise.catch).toEqual(jasmine.any(Function));
      });

      it(
        'resolve promise on api request success',
        angular.mock.inject(($rootScope, OperationsAPIClient) =>
      {
        let success = false,
            error   = false,
            responseData;

        OperationsAPIClient.changeRequestState(true);

        Operations[methodName](paramId).then((data) => {
          success = true;
          responseData = data;
        }, () => {
          error = true;
        });
        $rootScope.$apply();

        expect(success).toBe(true);
        expect(error).toBe(false);
        expect(responseData).toEqual(expectedData);
      }));

      it(
        'request api only once / use cache for next calls',
        angular.mock.inject(($rootScope, OperationsAPIClient) =>
      {
        let success = false,
            error   = false,
            responseData;

        OperationsAPIClient.changeRequestState(true);

        spyOn(OperationsAPIClient, apiMethodName).and.callThrough();
        spyOn(Operations, methodName).and.callThrough();

        Operations[methodName](paramId).then((data) => {
          success = true;
          responseData = data;
        }, () => {
          error = true;
        });
        $rootScope.$apply();

        expect(Operations[methodName]).toHaveBeenCalled();
        expect(OperationsAPIClient[apiMethodName]).toHaveBeenCalled();
        expect(success).toBe(true);
        expect(error).toBe(false);
        expect(responseData).toEqual(expectedData);

        success = false;
        error = false;
        responseData = null;

        Operations[methodName](paramId).then((data) => {
          success = true;
          responseData = data;
        }, () => {
          error = true;
        });
        $rootScope.$apply();

        expect(Operations[methodName]).toHaveBeenCalled();
        expect(Operations[methodName].calls.count()).toBe(2);
        expect(OperationsAPIClient[apiMethodName].calls.count()).toBe(1);
        expect(success).toBe(true);
        expect(error).toBe(false);
        expect(responseData).toEqual(expectedData);
      }));

      it(
        'reject promise on api request error',
        angular.mock.inject(($rootScope, OperationsAPIClient) =>
      {
        let success = false,
            error   = false;

        OperationsAPIClient.changeRequestState(false);

        Operations[methodName](paramId).then(() => {
          success = true;
        }, () => {
          error = true;
        });
        $rootScope.$apply();

        expect(success).toBe(false);
        expect(error).toBe(true);
      }));

      if (paramId) {
        it(
          'resolve promise with empty response for unknown id',
          angular.mock.inject(($rootScope, OperationsAPIClient) =>
        {
          let success = false,
              error   = false,
              responseData;

          OperationsAPIClient.changeRequestState(true);

          Operations[methodName]('xxx').then((data) => {
            success = true;
            responseData = data;
          }, () => {
            error = true;
          });
          $rootScope.$apply();

          expect(success).toBe(true);
          expect(error).toBe(false);
          expect(responseData).toBeNull();
        }));
      }
    });
  }

  testMethod('getAll', 'getAll', mockOperations);
  testMethod('get', 'getAll', mockOperations['id-02'], 'id-02');
  testMethod('getCatalog', 'getCatalog', mockCatalog);

});
