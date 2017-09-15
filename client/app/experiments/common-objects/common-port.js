/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

function Port(options) {
  this.nodeId = options.nodeId;
  this.index = options.portIndex;
  this.type = options.type;
  this.required = options.required;
  this.typeQualifier = options.typeQualifier;

  this.generateId();
}

/**
 * Generates port id.
 */
Port.prototype.generateId = function generateId() {
  this.id = this.type + '-' + this.index + '-' + this.nodeId;
};

module.exports = Port;
