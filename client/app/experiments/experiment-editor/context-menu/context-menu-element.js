/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

function ContextMenuElement() {
  return {
    restrict: 'E',
    scope: {
      icon: '=',
      description: '=',
      active: '=',
      href: '=',
      visible: '=',
      action: '&'
    },
    replace: true,
    templateUrl: 'app/experiments/experiment-editor/context-menu/context-menu-element.html'
  };
}

exports.inject = function (module) {
  module.directive('contextMenuElement', ContextMenuElement);
};
