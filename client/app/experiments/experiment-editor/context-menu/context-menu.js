/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

function ContextMenu() {
  return {
    restrict: 'E',
    replace: true,
    controller: ContextMenuController,
    controllerAs: 'contextMenuController',
    templateUrl: 'app/experiments/experiment-editor/context-menu/context-menu.html'
  };
}

/* @ngInject */
function ContextMenuController() {
  var that = this;
  var internal = {};

  internal.visible = false;
  internal.position = { x: 0, y: 0 };

  that.setPosition = function setPosition(parent, object) {
    internal.position.x = object.x - parent.x;
    internal.position.y = object.y - parent.y;
  };

  that.getPositionX = function getPositionX() {
    return internal.position.x;
  };

  that.getPositionY = function getPositionY() {
    return internal.position.y;
  };

  that.isVisible = function isVisible() {
    return internal.visible ? 'visible' : 'none';
  };

  that.setVisible = function setVisible() {
    internal.visible = true;
  };

  that.setInvisible = function setInvisible() {
    internal.visible = false;
  };

  return that;
}

exports.inject = function (module) {
  module.directive('contextMenu', ContextMenu);
};
