/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by Oleksandr Tserkovnyi on 22.06.15.
 */

'use strict';

var angular = require('angular');

let dsModule = angular.module('deepsense.node-parameters', []);

require('./deepsense-node-parameters.factory.js').inject(dsModule);