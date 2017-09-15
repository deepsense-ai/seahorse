'use strict';

angular.module('deepsense.graph-model').
  factory('Edge', function() {
    class Edge {
      constructor(options) {
        this.startNodeId = options.startNodeId;
        this.endNodeId = options.endNodeId;
        this.startPortId = options.startPortId;
        this.endPortId = options.endPortId;
        this.state = Edge.STATE_TYPE.UNKNOWN;
        this.id = this.generateId();
      }

      generateId() {
        return `${this.startNodeId}#${this.startPortId}_${this.endNodeId}#${this.endPortId}`;
      }

      serialize() {
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
      }

      static get STATE_TYPE() {
        return {
          'ALWAYS': 'Edge.STATE_TYPE.ALWAYS',
          'MAYBE': 'Edge.STATE_TYPE.MAYBE',
          'NEVER': 'Edge.STATE_TYPE.NEVER',
          'UNKNOWN': 'Edge.STATE_TYPE.UNKNOWN'
        };
      }

      static get CREATE() { return 'Edge.CREATE'; }
      static get REMOVE() { return 'Edge.REMOVE'; }
      static get DRAG() { return 'Edge.DRAG'; }
    }

    return Edge;
  });