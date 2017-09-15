/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
/*global console*/
'use strict';


/**
 * Provides orm handler for UI data.
 *
 * @param {Function} callback
 */
module.exports = function ormHandler(callback) {
  var disk = require('sails-disk'),
      Waterline = require('waterline'),
      orm = new Waterline(),
      config = {
        adapters: {
          diskDB: disk,
        },
        connections: {
          api: {
            adapter: 'diskDB'
          }
        }
      };

  orm.loadCollection(require('./experimentModel.js'));
  orm.initialize(config, function(error) {
    if (error) {
      console.error('error:', 'orm', error);
    }
    callback(orm);
  });
};
