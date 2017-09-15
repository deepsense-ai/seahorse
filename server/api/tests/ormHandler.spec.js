/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
'use strict';


describe('ORM handler', () => {
  var config = {};

  it('should fire callback after load', () => {
    let loaded = false;

    runs(() => {
      require('./../ormHandler.js')((orm) => {
        config.orm = orm;
        loaded = true;
      });
    });

    waitsFor(() => loaded, 1000);

    runs(() => {
      expect(loaded).toBe(true);
      expect(config.orm).toEqual(jasmine.any(Object));

    });
  });

  it('should have some loaded collections', () => {
    expect(Object.keys(config.orm.collections).length).toBeGreaterThan(0);
  });

});
