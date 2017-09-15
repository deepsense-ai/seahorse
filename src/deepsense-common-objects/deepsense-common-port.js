'use strict';

angular.module('deepsense.graph-model').
  factory('Port', function() {

    function Port(options) {
      this.nodeId = options.nodeId;
      this.index = options.portIndex;
      this.type = options.type;
      this.required = options.required;
      this.typeQualifier = options.typeQualifier;
      this.generateId();
    }

    Port.prototype.generateId = function generateId() {
      this.id = this.type + '-' + this.index + '-' + this.nodeId;
    };

    return Port;
  });