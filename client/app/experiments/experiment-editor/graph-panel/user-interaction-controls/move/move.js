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
      relatedTo: '@',
      width: '=',
      height: '=',
      top: '=',
      left: '='
    },
    replace: true,
    transclude: true,
    controller: MoveController,
    controllerAs: 'moveController',
    templateUrl: 'app/experiments/experiment-editor/graph-panel/user-interaction-controls/user-interaction-controls-element.html'
  };
}

/* @ngInject */
function MoveController($document, $scope) {
  var that = this;
  var internal = {};

  (internal.settings = {}).css = {
    width:  $scope.width * 0.75,
    height: $scope.height * 0.75,
    top:    $scope.top,
    left:   $scope.left
  };
  internal.settings.initialStyles = {
    position: 'absolute',
    width: internal.settings.css.width + 'px',
    height: internal.settings.css.height + 'px',
    top: internal.settings.css.top + 'px',
    left: internal.settings.css.left + 'px'
  };
  internal.spacePressed = false;
  internal.itemActivated = false;
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
    //var movableElementPosition = internal.relatedToElement.getBoundingClientRect();
    //var staticElementWidth = internal.relatedToElement.clientWidth;
    // TODO make for height the same.
    //var actualShift = (staticElementWidth - movableElementPosition.width) / 2; // 3000 - 1411 / 2;

    if (!isReverse) {
      // TODO: temporary
      internal.settings.css[direction] = 0;
    }
  };

  internal.preventMovingBeyondViewPort = function preventMovingBeyondViewPort (direction, isReverse) {
    var movableElementPosition = internal.relatedToElement.getBoundingClientRect();
    var blockerPosition = internal.viewPort.getBoundingClientRect();
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
    //console.log(`${direction.toUpperCase()}? `, eventMovementValue < 0, eventMovementValue);

    if (eventMovementValue < 0) {
      if (internal.preventMovingBeyondViewPort(direction, internal.REVERSE) === false) {
        internal.settings.css[direction] -= subtraction;
      } else {
        internal.setMaximumPosition(direction, internal.REVERSE);
      }
    } else if (eventMovementValue > 0) {
      if (internal.preventMovingBeyondViewPort(direction) === false) {
        internal.settings.css[direction] += -subtraction;
      } else {
        internal.setMaximumPosition(direction);
      }
    }
  };

  internal.setPosition = function setPosition (direction, value) {
    internal.relatedToElement.style[direction] = value + 'px';
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

    if (internal.spacePressed || internal.itemActivated) {
      $scope.active = true;
      internal.startCoordinates = {
        y: event.pageY,
        x: event.pageX
      };
      internal.relatedToElement.classList.add('moving');
      internal.relatedToElement.addEventListener('mousemove', internal.move);
    }
  };

  internal.moveEndListener = function moveEndListener (event) {
    if (internal.itemActivated === false) {
      $scope.active = false;
      $scope.$apply();
    }
    internal.relatedToElement.classList.remove('moving');
    internal.relatedToElement.removeEventListener('mousemove', internal.move);
  };

  internal.updateState = function updateState () {
    internal.settings.css.left  =  internal.relatedToElement.offsetLeft;
    internal.settings.css.top   =  internal.relatedToElement.offsetTop;
  };

  internal.init = function init () {
    $(internal.relatedToElement).css(internal.settings.initialStyles);
    internal.relatedToElement.addEventListener('mousedown', internal.moveStartListener);

    $document.on('mouseup', internal.moveEndListener);
    $document.on('keydown', internal.keyDownListener);
    $document.on('keyup', internal.keyUpListener);

    $scope.$on('GraphPanel.CENTERED', internal.updateState);
    $scope.$on('GraphPanel.ZERO', internal.updateState);

    $scope.$on('$destroy', () => {
      $document.off('mouseup', internal.moveEndListener);
      $document.off('keydown', internal.keyDownListener);
      $document.off('keyup', internal.keyUpListener);
    });
  };

  $scope.activateItem = function activateItem () {
    internal.relatedToElement.classList.toggle('forceMoving');
    internal.itemActivated = !internal.itemActivated;
    $scope.active = internal.itemActivated;
  };

  internal.relatedToElement = $document[0].querySelector($scope.relatedTo);
  internal.viewPort = internal.relatedToElement.parentNode;
  internal.init();

  return that;
}

exports.inject = function (module) {
  module.directive('dsMove', Move);
};
