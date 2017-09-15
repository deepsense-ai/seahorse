/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

/* @ngInject */
function DragAndDrop($rootScope, $log, MouseEvent) {
  var that = this;
  var internal = {
    allDraggableElements: new WeakMap(),
    draggableElement: {},
    states: {
      'default': 'default',
      'displaced': 'displaced'
    }
  };

  that.drag = drag;
  that.drop = drop;

  function drag(event, element) {
    let type = event.dataTransfer.getData('draggableType');

    setCurrentElement(element);

    switch (type) {
      case 'frame':
        saveFrameElementData(...arguments);
        break;
      case 'exact':
        saveExactElementData(...arguments);
        break;
      default:
        $log.error('Unknown type %s', type);
    }

    $rootScope.$broadcast('Drag.START', event, element);
  }

  function drop(event, element) {
    let type = event.dataTransfer.getData('draggableType');

    switch (type) {
      case 'frame':
        handleFrameMovement(...arguments);
        break;
      case 'exact':
        publishExactEvent(event, internal.draggableElement.element);
        break;
      default:
        $log.error('Unknown type %s', type);
    }

    $rootScope.$broadcast('Drop.DROP', ...arguments);
  }

  function saveFrameElementData(event, element) {
    let containerStartEventCoordinates =
      MouseEvent.getEventOffsetOfElement(event, element[0]);

    internal.draggableElement.element = element;
    internal.draggableElement.eventPosition =
      internal.draggableElement.eventPosition || {
        x: event.pageX,
        y: event.pageY
      };

    /* first drag */
    if (!internal.draggableElement.containerStartEventCoordinates) {
      internal.draggableElement.containerStartEventCoordinates =
        containerStartEventCoordinates;
    }

    /* set correct point of drag based on old point */
    internal.draggableElement.eventPosition.x +=
      containerStartEventCoordinates.x -
      internal.draggableElement.containerStartEventCoordinates.x;

    internal.draggableElement.eventPosition.y +=
      containerStartEventCoordinates.y -
      internal.draggableElement.containerStartEventCoordinates.y;

    /* save new point of drag as old */
    internal.draggableElement.containerStartEventCoordinates =
      containerStartEventCoordinates;
  }

  function saveExactElementData(event, element) {
    internal.draggableElement.element = element;
    internal.draggableElement.exactType =
      event.dataTransfer.getData('draggableExactType');
  }

  function handleFrameMovement(event, element) {
    internal.draggableElement.element
      .css('transform', `translate(
        ${event.pageX - internal.draggableElement.eventPosition.x}px,
        ${event.pageY - internal.draggableElement.eventPosition.y}px
      )`);

    // TODO return to default place
    internal.draggableElement.element
      .attr('data-drag-state', internal.states.displaced);
  }

  function publishExactEvent(event, element) {
    $rootScope.$broadcast('Drop.EXACT',
      ...arguments, internal.draggableElement.exactType);
  }

  function setCurrentElement(key) {
    var draggableElementFromList = internal.allDraggableElements.get(key);

    if (draggableElementFromList) {
      internal.draggableElement = draggableElementFromList;
    } else {
      internal.draggableElement = {};
      internal.allDraggableElements.set(key, internal.draggableElement);
    }
  }
}

exports.function = DragAndDrop;

exports.inject = function(module) {
  module.service('DragAndDrop', DragAndDrop);
};
