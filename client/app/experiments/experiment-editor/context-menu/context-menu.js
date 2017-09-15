/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

function ContextMenu() {
  return {
    restrict: 'E',
    replace: true,
    scope: {
      elements: '=',
      containerX: '=',
      containerY: '=',
      positionX: '=',
      positionY: '=',
      state: '='
    },
    controller: ContextMenuController,
    controllerAs: 'contextMenuController',
    templateUrl: 'app/experiments/experiment-editor/context-menu/context-menu.html',
    link: function (scope, element, attribtues, controller) {
      scope.$watch('state', function(newValue) {
        if (newValue === 'visible') {
          controller.open();
        } else {
          controller.close();
        }
      });
    }
  };
}

/* @ngInject */
function ContextMenuController($scope) {
  var that = this;
  var internals = {};

  internals.visible = false;
  internals.position = {x: 0, y: 0};

  that.open = function open() {
    that.setPosition(
      {
        x: $scope.containerX,
        y: $scope.containerY
      },
      {
        x: $scope.positionX,
        y: $scope.positionY
      });
    internals.visible = true;
  };

  that.close = function close () {
    internals.visible = false;
  };

  that.setPosition = function setPosition(parent, object) {
    internals.position.x = object.x - parent.x;
    internals.position.y = object.y - parent.y;
  };

  that.getPositionX = function getPositionX() {
    return internals.position.x;
  };

  that.getPositionY = function getPositionY() {
    return internals.position.y;
  };

  that.getDisplay = function getDisplay() {
    return internals.visible ? 'block' : 'none';
  };

  return that;
}

exports.inject = function (module) {
  module.directive('contextMenu', ContextMenu);
};
