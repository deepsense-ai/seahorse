/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

function Edge(options) {
  this.startNodeId = options.startNodeId;
  this.endNodeId = options.endNodeId;
  this.startPortId = options.startPortId;
  this.endPortId = options.endPortId;
  this.id = this.generateId();
}

/**
 * Generates node id.
 */
Edge.prototype.generateId = function generateId() {
  return this.startNodeId + '#' + this.startPortId + '_' + this.endNodeId + '#' + this.endPortId;
};

/**
 * Serializes edge data to transfer format.
 *
 * @return {object}
 */
Edge.prototype.serialize = function serialize() {
  let data = {
    'from': {
      'nodeId': this.startNodeId,
      'portIndex': this.startPortId
    },
    'to': {
      'nodeId': this.endNodeId,
      'portIndex': this.endPortId
    }
  };

  return data;
};

Edge.CREATE = 'Edge.CREATE';
Edge.REMOVE = 'Edge.REMOVE';

module.exports = Edge;
