/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
'use strict';


describe('API proxy', () => {
  var proxy = require('./../apiProxy.js');

  it('should be a function', () => {
    expect(proxy).toEqual(jasmine.any(Function));
  });

});
