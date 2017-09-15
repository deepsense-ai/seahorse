'use strict';

const attributesPanel = angular.module('deepsense.attributes-panel', [
  'deepsense.spinner',
  'deepsense.node-parameters',
  'ui.bootstrap',
  'xeditable',
  'NgSwitchery',
  'ui.ace',
  'angucomplete-alt'
]).run((editableOptions) => editableOptions.theme = 'bs3');

require('./attribute-types/attribute-types.js');
require('./attributes-list/attributes-list.js');
require('./attributes-panel/attributes-panel.js');
require('./color-picker/color-picker.js');
require('./common/common.js');

module.exports = attributesPanel;
