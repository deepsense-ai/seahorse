/**
 * Copyright (c) 2015, CodiLime Inc.
 */

'use strict';

angular.module('deepsense.graph-model').
  factory('Edge', function() {
    function Edge(options) {
      this.startNodeId = options.startNodeId;
      this.endNodeId = options.endNodeId;
      this.startPortId = options.startPortId;
      this.endPortId = options.endPortId;
      this.id = this.generateId();
      this.state = this.STATE_TYPE.UNKNOWN;
    }

    /**
     * Generates edge id.
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
      return {
        'from': {
          'nodeId': this.startNodeId,
          'portIndex': this.startPortId
        },
        'to': {
          'nodeId': this.endNodeId,
          'portIndex': this.endPortId
        }
      };
    };

    Edge.STATE_TYPE = {
      'ALWAYS': 'Edge.STATE_TYPE.ALWAYS',
      'MAYBE': 'Edge.STATE_TYPE.MAYBE',
      'NEVER': 'Edge.STATE_TYPE.NEVER',
      'UNKNOWN': 'Edge.STATE_TYPE.UNKNOWN'
    };

    Edge.prototype.STATE_TYPE = Edge.STATE_TYPE;

    Edge.CREATE = 'Edge.CREATE';
    Edge.REMOVE = 'Edge.REMOVE';
    Edge.DRAG = 'Edge.DRAG';

    return Edge;
  });