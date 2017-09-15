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
 * Sets edge id.
 *
 * @param id {string}
 */
Edge.prototype.setId = function setId(id) {
  this.id = id;
};

/**
 * Returns edge id.
 *
 * @return {string}
 */
Edge.prototype.getId = function getId() {
  return this.id;
};


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
