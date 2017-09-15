'use strict';

angular.module('deepsense.attributes-panel', [
  'deepsense.spinner',
  'deepsense.node-parameters',
  'NgSwitchery',
  'ui.bootstrap',
  'xeditable'
]).run((editableOptions) => editableOptions.theme = 'bs3');
