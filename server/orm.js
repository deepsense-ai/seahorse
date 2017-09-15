/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
'use strict';

var Waterline = require('waterline');
var waterline = new Waterline();
var disk = require('sails-disk');

var example = require('./models/example.js')(waterline);
waterline.loadCollection(example);

var config = {
  adapters: {
    'default': disk,
    disk: disk
  },
  connections: {
    disk: {
      adapter: 'disk'
    }
  },
  defaults: {
    migrate: 'alter'
  }
};

module.exports = function(callback) {
  waterline.initialize(config, function(err, models) {
    if(err) {
      throw new Error(err.message);
    }
    callback(waterline);
  });
};
