/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
/*global inject*/
'use strict';


describe('OperationsAPIClient', () => {
  var module,
      OperationsAPIClient;

  beforeEach(() => {
    module = angular.module('test', []);
    require('../factory.BaseAPIClient.js').inject(module);
    require('../factory.OperationsAPIClient.js').inject(module);

    angular.mock.module('test');
    inject((_OperationsAPIClient_) => {
      OperationsAPIClient = _OperationsAPIClient_;
    });
  });


  it('should be defined', () => {
    expect(OperationsAPIClient).toBeDefined();
    expect(OperationsAPIClient).toEqual(jasmine.any(Object));
  });

});
