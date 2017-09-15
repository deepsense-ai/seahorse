'use strict';

angular.module('deepsense.graph-model').
  factory('Port', function() {
    class Port {
      constructor(options) {
        this.nodeId = options.nodeId;
        this.index = options.portIndex;
        this.type = options.type;
        this.required = options.required;
        this.portPosition = options.portPosition;
        this.typeQualifier = options.typeQualifier;
        this.id = this.generateId();
      }

      generateId() {
        return `${this.type}-${this.index}-${this.nodeId}`;
      }
    }

    return Port;
  });
