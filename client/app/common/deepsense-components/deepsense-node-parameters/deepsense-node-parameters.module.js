/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by Oleksandr Tserkovnyi on 22.06.15.
 */

'use strict';

const angular = require('angular');
const nodeParams = angular.module('deepsense.node-parameters', []);

require('./deepsense-node-parameters.factory.js').inject(nodeParams);

module.exports = nodeParams;
