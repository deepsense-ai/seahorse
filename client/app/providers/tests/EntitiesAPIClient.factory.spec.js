/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
/*global inject*/
'use strict';


describe('EntitiesAPIClient', () => {
  var module,
      EntitiesAPIClient;

  beforeEach(() => {
    module = angular.module('test', []);
    require('../BaseAPIClient.factory.js').inject(module);
    require('../EntitiesAPIClient.factory.js').inject(module);

    angular.mock.module('test');
    inject((_EntitiesAPIClient_) => {
      EntitiesAPIClient = _EntitiesAPIClient_;
    });
  });


  it('should be defined', () => {
    expect(EntitiesAPIClient).toBeDefined();
    expect(EntitiesAPIClient).toEqual(jasmine.any(Object));
  });


});
