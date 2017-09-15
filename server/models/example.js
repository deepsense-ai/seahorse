/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

var Waterline = require('waterline');

module.exports = function(waterline) {
  return Waterline.Collection.extend({
    identity: 'example',
    connection: 'disk',
    attributes: {
      name  : { type: 'string' }
    }
  });
};
