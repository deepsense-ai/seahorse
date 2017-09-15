/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
/*global inject*/
'use strict';


describe('ExperimentAPIClient', () => {
  var module,
      ExperimentAPIClient;

  beforeEach(() => {
    module = angular.module('test', []);
    require('../factory.BaseAPIClient.js').inject(module);
    require('../factory.ExperimentAPIClient.js').inject(module);

    angular.mock.module('test');
    inject((_ExperimentAPIClient_) => {
      ExperimentAPIClient = _ExperimentAPIClient_;
    });
  });


  it('should be defined', () => {
    expect(ExperimentAPIClient).toBeDefined();
    expect(ExperimentAPIClient).toEqual(jasmine.any(Object));
  });


  describe('should have getList method', () => {
    var $httpBackend,
        mockRequest;

    var url = '/api/experiments',
        response = {'test': true};

    beforeEach(() => {
      inject(($injector) => {
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
      expect(ExperimentAPIClient.getList).toEqual(jasmine.any(Function));
    });

    it('which return promise', () => {
      $httpBackend.expectGET(url);

      let promise = ExperimentAPIClient.getList();
      expect(promise).toEqual(jasmine.any(Object));
      expect(promise.then).toEqual(jasmine.any(Function));

      $httpBackend.flush();
    });

    it('which return promise & resolve on request success', () => {
      $httpBackend.expectGET(url);

      let promise = ExperimentAPIClient.getList();
      $httpBackend.flush();

      promise.then((data) => {
        expect(data).toEqual(response);
      });
    });

  });

});
