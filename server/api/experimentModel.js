/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
'use strict';

var Waterline = require('waterline');


/**
 * ORM model for UI data.
 */
var Experiment = Waterline.Collection.extend({
  identity: 'experiment',
  connection: 'api',
  attributes: {
    id: {
      type: 'string',
      autoPK: false,
      index: true,
      requred: true
    },
    nodes: {
      type: 'json'
    }
  }
});

module.exports = Experiment;
