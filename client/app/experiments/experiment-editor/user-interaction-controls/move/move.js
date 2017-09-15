/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

function Move() {
  return {
    restrict: 'E',
    scope: {
      text: '@',
      classSet: '@',
      icon: '@',
      elementToMove: '@',
      width: '=',
      height: '=',
      top: '=',
      left: '='
    },
    replace: true,
    controller: MoveController,
    controllerAs: 'moveController',
    templateUrl: 'app/experiments/experiment-editor/user-interaction-controls/user-interaction-controls-element.html'
  };
}

/* @ngInject */
function MoveController($document, $scope, $rootScope, GraphPanelRendererService) {
  var that = this;
  var internal = {};

  (internal.settings = {}).css = {
    width:  $scope.width,
    height: $scope.height,
    top:    $scope.top,
    left:   $scope.left
  };
  internal.settings.initialStyles = `
    position: absolute;
    width: ${internal.settings.css.width}px;
    height: ${internal.settings.css.height}px;
    top: ${internal.settings.css.top}px;
    left: ${internal.settings.css.left}px;
  `;
  internal.spacePressed = false;
  internal.forced = false;
  internal.REVERSE = true;

  internal.isSpacePressed = function isSpacePressed (event) {
    if (event.keyCode === 0 || event.keyCode === 32) {
      return true;
    }
  };

  internal.keyDownListener = function keyDownListener (event) {
    if (internal.isSpacePressed(event)) {
      event.preventDefault();
      internal.spacePressed = true;
    }
  };

  internal.keyUpListener = function keyUpListener (event) {
    internal.spacePressed = false;
    internal.moveEndListener();
  };

  internal.getEventSubtraction = function getEventSubtraction (event) {
    return {
      y: internal.startCoordinates.y - event.pageY,
      x: internal.startCoordinates.x - event.pageX
    };
  };

  /**
   * Return after moving beyond border
   */
  internal.setMaximumPosition = function setMaximumPosition (direction, isReverse) {
    var movableElementPosition = internal.elementToMove.getBoundingClientRect();
    var staticElementWidth = internal.elementToMove.clientWidth;
    // TODO make for height the same.
    var actualShift = (staticElementWidth - movableElementPosition.width) / 2; // 3000 - 1411 / 2;

    if (!isReverse) {
      internal.settings.css[direction] = -actualShift; // 0
    }
  };

  internal.preventMovingBeyondParent = function preventMovingBeyondParent (direction, isReverse) {
    var movableElementPosition = internal.elementToMove.getBoundingClientRect();
    var blockerPosition = internal.elementToMoveParent.getBoundingClientRect();
    var directionToCheck = direction;
    var preventMoving = false;

    if (isReverse) {
      switch (direction) {
        case 'left':
          directionToCheck = 'right';
          break;
        case 'top':
          directionToCheck = 'bottom';
          break;
      }
    }

    if (!isReverse && movableElementPosition[directionToCheck] > blockerPosition[directionToCheck]) {
      preventMoving = true;
    } else if (isReverse && movableElementPosition[directionToCheck] < blockerPosition[directionToCheck]) {
      preventMoving = true;
    }

    return preventMoving;
  };

  internal.setMovement = function setMovement (direction, subtraction, eventMovementValue) {
    console.log(`${direction.toUpperCase()}? `, eventMovementValue < 0, eventMovementValue);

    if (eventMovementValue < 0) {
      if (internal.preventMovingBeyondParent(direction, internal.REVERSE) === false) {
        internal.settings.css[direction] -= subtraction;
      } else {
        internal.setMaximumPosition(direction, internal.REVERSE);
      }
    } else if (eventMovementValue > 0) {
      if (internal.preventMovingBeyondParent(direction) === false) {
        internal.settings.css[direction] += -subtraction;
      } else {
        internal.setMaximumPosition(direction);
      }
    }
  };

  internal.setPosition = function setPosition (direction, value) {
    internal.elementToMove.style[direction] = value + 'px';
  };

  internal.move = function move (event) {
    var subtraction = internal.getEventSubtraction(event);

    internal.setMovement('top',   subtraction.y, event.movementY);
    internal.setMovement('left',  subtraction.x, event.movementX);

    internal.setPosition('top', internal.settings.css.top);
    internal.setPosition('left', internal.settings.css.left);

    internal.startCoordinates.y = event.pageY;
    internal.startCoordinates.x = event.pageX;
  };

  internal.moveStartListener = function moveStartListener (event) {
    // prevent selecting
    event.preventDefault();

    if (internal.spacePressed || internal.forced) {
      $scope.active = true;
      internal.startCoordinates = {
        y: event.pageY,
        x: event.pageX
      };
      internal.elementToMove.classList.add('moving');
      internal.elementToMove.addEventListener('mousemove', internal.move);
    }
  };

  internal.moveEndListener = function moveEndListener (event) {
    if (internal.forced === false) {
      $scope.active = false;
      $scope.$apply();
    }
    internal.elementToMove.classList.remove('moving');
    internal.elementToMove.removeEventListener('mousemove', internal.move);
  };

  internal.updateState = function updateState () {
    internal.settings.css.left  =  internal.elementToMove.offsetLeft;
    internal.settings.css.top   =  internal.elementToMove.offsetTop;
  };

  internal.init = function init () {
    internal.elementToMove.setAttribute('style', internal.settings.initialStyles);
    internal.elementToMove.addEventListener('mousedown', internal.moveStartListener);

    $document.on('mouseup', internal.moveEndListener);
    $document.on('keydown', internal.keyDownListener);
    $document.on('keyup', internal.keyUpListener);

    // if zoom is connected and min zoom reached
    $rootScope.$on('Zoom.MIN_REACHED', GraphPanelRendererService.setCenter);
    $rootScope.$on('GraphPanel.CENTERED', internal.updateState);
    $rootScope.$on('GraphPanel.ZERO', internal.updateState);

    $scope.$on('$destroy', () => {
      $document.off('mouseup', internal.moveEndListener);
      $document.off('keydown', internal.keyDownListener);
      $document.off('keyup', internal.keyUpListener);
    });
  };

  $scope.force = function force () {
    internal.elementToMove.classList.toggle('forceMoving');
    internal.forced = !internal.forced;
    $scope.active = internal.forced;
  };

  internal.elementToMove = $document[0].querySelector($scope.elementToMove);
  internal.elementToMoveParent = internal.elementToMove.parentNode;
  internal.init();

  return that;
}

exports.inject = function (module) {
  module.directive('dsMove', Move);
};
