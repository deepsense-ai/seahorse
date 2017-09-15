/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Konrad Szałwiński
 */

'use strict';

/* @ngInject */
function Droppable() {
  return {
    restrict: 'A',
    link: function (scope, element, attrs) {
      angular.element(element).attr('draggable', 'true');
      element.on('dragstart',function (event) {
        event.dataTransfer.effectAllowed = 'move';
        event.dataTransfer.setData('elementId', element[0].id);
        event.dataTransfer.setData('droppable', true);
      });
    }
  };
}

Droppable.DROP = 'Droppable.DROP';

exports.inject = function (module) {
  module.directive('droppable', Droppable);
};
