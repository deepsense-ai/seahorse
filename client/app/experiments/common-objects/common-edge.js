/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

function Edge(options) {
  this.startNodeId = options.startNodeId;
  this.endNodeId = options.endNodeId;
  this.startPortId = options.startPortId;
  this.endPortId = options.endPortId;
}

/**
 * Serializes edge data to transfer format.
 *
 * @return {object}
 */
Edge.prototype.serialize = function serialize() {
  let data = {
    'from': {
      'node': this.startNodeId,
      'portIndex': this.startPortId
    },
    'to': {
      'node': this.endNodeId,
      'portIndex': this.endPortId
    }
  };

  return data;
};

module.exports = Edge;
