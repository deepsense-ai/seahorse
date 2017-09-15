'use strict';

require('./../deepsense-node-parameters/deepsense-node-parameters.module.js');

const graphModel = angular.module('deepsense.graph-model', ['deepsense.node-parameters']);

require('./deepsense-common-objects/deepsense-common-graph-node.js');
require('./deepsense-common-objects/deepsense-common-port.js');
require('./deepsense-common-objects/deepsense-common-edge.js');
require('./deepsense-common-objects/deepsense-common-workflow.js');

module.exports = graphModel;
