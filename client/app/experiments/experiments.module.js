/**
 * Copyright (c) 2015, CodiLime Inc.
 */

'use strict';

var angular = require('angular');

var experiments = angular.module('ds.experiments', []);
require('./experiment.drawing.service.js').inject(experiments);
require('./experiment-list.controller.js').inject(experiments);
require('./experiment.controller.js').inject(experiments);
require('./experiments.config.js').inject(experiments);
require('./experiment.factory.js').inject(experiments);
require('./directives/draggable.directive.js').inject(experiments);
require('./directives/onRenderFinish.directive.js').inject(experiments);
require('./directives/graphNode.directive.js').inject(experiments);
require('./directives/operationAttributesView.directive.js').inject(experiments);
module.exports = experiments;
