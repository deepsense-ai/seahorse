/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
'use strict';


describe('API config', () => {
  var config = require('./../apiConfig.js');

  it('should be an object', () => {
    expect(config).toEqual(jasmine.any(Object));
  });

  it('should have propper url property', () => {
    expect(typeof config.url).toBe('string');
    expect(config.url).toMatch(/^http.+/);
  });

  it('should have propper version property', () => {
    expect(typeof config.version).toBe('string');
  });

  it('should have valid resources list', () => {
    let resources = config.resources;

    expect(resources).toEqual(jasmine.any(Object));
    expect(Object.keys(resources).length).toBeGreaterThan(1);
  });

});
