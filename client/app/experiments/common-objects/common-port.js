/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

function Port(options) {
  this.id = this.generateId(options);
  this.index = options.portIndex;
  this.required = options.required;
  this.typeQualifier = options.typeQualifier;
}

/**
 * Generates port id.
 *
 * @param {object} options
 *
 * @return {string}
 */
Port.prototype.generateId = function generateId(options) {
  return options.type + '-' + options.portIndex + '-' + options.nodeId;
};

module.exports = Port;
