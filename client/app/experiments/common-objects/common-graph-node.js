/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

var Port = require('./common-port.js');

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
  this.x = options.x;
  this.y = options.y;
  this.parameters = options.parameters;
  this.setStatus(options.state);
}

GraphNode.prototype.STATUS = {
  'INDRAFT':   'status_indraft',
  'QUEUED':    'status_queued',
  'RUNNING':   'status_running',
  'COMPLETED': 'status_completed',
  'FAILED':    'status_failed',
  'ABORTED':   'status_aborted'
};
GraphNode.prototype.STATUS_DEFAULT = GraphNode.prototype.STATUS.INDRAFT;

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

/**
 * Serializes node data to transfer format.
 *
 * @return {object}
 */
GraphNode.prototype.serialize = function serialize() {
  let data = {
    'id': this.id,
    'operation': {
      'id': this.operationId,
      'name': this.name,
      'version': this.version
    },
    'parameters': this.parameters.serialize(),
    'ui': {
      'x': this.x,
      'y': this.y
    }
  };

  return data;
};

/**
 * Sets graph node launch status.
 *
 * @param {object} state
 */
GraphNode.prototype.setStatus = function setStatus(state) {
  if (state && state.status && Object.keys(this.STATUS).indexOf(state.status) > -1) {
    this.status = this.STATUS[state.status];
  } else if (!this.status) {
    this.status = this.STATUS_DEFAULT;
  }
};

GraphNode.CLICK = 'GraphNode.CLICK';
GraphNode.MOVE = 'GraphNode.MOVE';

module.exports = GraphNode;
