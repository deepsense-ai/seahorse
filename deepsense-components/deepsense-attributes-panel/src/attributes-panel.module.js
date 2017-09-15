'use strict';

angular.module('deepsense.attributes-panel', [
  'deepsense.spinner',
  'deepsense.node-parameters',
  'NgSwitchery',
  'ui.bootstrap',
  'xeditable',
  'ui.ace',
  'angucomplete-alt'
]).run((editableOptions) => editableOptions.theme = 'bs3');
