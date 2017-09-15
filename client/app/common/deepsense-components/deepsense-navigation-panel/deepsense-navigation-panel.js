/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by: Grzegorz Swatowski
 */

'use strict';

const navigationPanel = angular.module('deepsense.navigation-panel', [
  'rt.debounce'
]);

require('./actions/deepsense-interactive.js');
require('./ui-widgets/deepsense-interaction-toolbar.js');

module.exports = navigationPanel;
