'use strict';

angular.module('deepsense.graph-model').
  factory('GraphNode', /*@ngInject*/function(Port) {

    function GraphNode(options) {
      this.name = options.name;
      this.id = options.id;
      this.operationId = options.operationId;
      this.version = options.version;
      this.icon = options.icon;
      this.type = options.type;
      this.description = options.description;
      this.input = this.fetchPorts('input', options.input);
      this.output = this.fetchPorts('output', options.output);
      this.edges = {};
      this.results = [];
      this.x = options.x;
      this.y = options.y;
      if (options.parametersValues) {
        this.parametersValues = options.parametersValues;
      } else {
        this.parameters = options.parameters;
      }
      this.stateDetails = _.clone(options.state);
      this.setStatus(options.state);
    }

    GraphNode.prototype.STATUS = {
      'DRAFT':     'status_draft',
      'QUEUED':    'status_queued',
      'RUNNING':   'status_running',
      'COMPLETED': 'status_completed',
      'FAILED':    'status_failed',
      'ABORTED':   'status_aborted'
    };

    GraphNode.prototype.STATUS_DEFAULT = GraphNode.prototype.STATUS.DRAFT;

    GraphNode.prototype.fetchPorts = function fetchPorts(type, ports) {
      var array = [];
      for (var i = 0; i < ports.length; i++) {
        var port = new Port({
          nodeId: this.id,
          type: type,
          portIndex: ports[i].portIndex,
          required: ports[i].required,
          typeQualifier: ports[i].typeQualifier
        });
        array.push(port);
      }
      return array;
    };

    GraphNode.prototype.serialize = function serialize() {
      let data = {
        'id': this.id,
        'operation': {
          'id': this.operationId,
          'name': this.name,
          'version': this.version
        },
        'parameters': this.parametersValues ? this.parametersValues : this.parameters.serialize(),
        'ui': {
          'x': this.x,
          'y': this.y
        }
      };

      return data;
    };

    GraphNode.prototype.setStatus = function setStatus(state) {
      if (state && state.status && Object.keys(this.STATUS).indexOf(state.status) > -1) {
        this.status = this.STATUS[state.status];
      } else if (!this.status) {
        this.status = this.STATUS_DEFAULT;
      }
    };

    GraphNode.prototype.updateState = function updateState(state) {
      this.results = state.results || [];
      this.setStatus(state);
    };

    GraphNode.prototype.getResult = function getResult(portIndex) {
      return this.results[portIndex];
    };

    GraphNode.prototype.setParameters = function setParameters(parametersSchema, DeepsenseNodeParameters) {
      if (this.parametersValues) {
        this.parameters = DeepsenseNodeParameters.factory.createParametersList(this.parametersValues, parametersSchema);
        this.parametersValues = null;
      }
    };

    GraphNode.prototype.hasParameters = function hasParameters() {
      return !this.parametersValues && this.parameters;
    };

    GraphNode.CLICK = 'GraphNode.CLICK';
    GraphNode.MOVE = 'GraphNode.MOVE';
    GraphNode.MOUSEDOWN = 'GraphNode.MOUSEDOWN';

    return GraphNode;
  });