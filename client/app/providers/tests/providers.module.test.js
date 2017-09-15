/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
'use strict';


describe('Providers module', () => {
  require('../providers.module.js');

  var module = angular.module('ds.providers');


  it('should be defined', () => {
    expect(module).toBeDefined();
  });

  it('should have registered items', () => {
    expect(module._invokeQueue.length).toBeGreaterThan(0);
  });

});
