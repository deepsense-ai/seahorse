/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
'use strict';


describe('Experiment model', () => {
  var model = require('./../experimentModel.js');

  it('should be an object', () => {
    expect(model).toEqual(jasmine.any(Function));
  });

});
