'use strict';

/* @ngInject */
function MultiSelection(GraphNode, MouseEvent, WorkflowService,
  $document, $timeout, $rootScope,
  debounce) {
  return {
    restrict: 'A',
    link: (scope, element) => {
      const CLASS_NAME = 'selection-element';
      let currentPoint = {};
      var selectionElement = $document[0].createElement('div');
      var $selectionElement = $(selectionElement);
      var startPoint = {
        x: 0,
        y: 0
      };
      var elementDimensions;
      var axisToWord = {
        x: {
          original: 'left',
          opposite: 'right',
          dimension: 'width'
        },
        y: {
          original: 'top',
          opposite: 'bottom',
          dimension: 'height'
        }
      };
      var inSelection = [];
      var nodeDimensions;
      var workflowNodes;
      var disabled;

      var startPainting = function startPainting(event) {
        /**
         * event.button 0: Main button pressed, usually the left button
         * https://developer.mozilla.org/en-US/docs/Web/API/MouseEvent/button
         */
        if (disabled || event.button !== 0) {
          return false;
        }

        startPoint = MouseEvent.getEventOffsetOfElement(event, element[0]);

        workflowNodes = workflowNodes || _.map(WorkflowService.getWorkflow()
          .getNodes(),
          node => {
            return {
              x: node.x,
              y: node.y,
              id: node.id
            };
          }
        );

        elementDimensions = elementDimensions || {
          width: element[0].clientWidth,
          height: element[0].clientHeight
        };
        nodeDimensions = nodeDimensions || {
          width: $(element[0].querySelector('[id^="node-"]'))
            .outerWidth(true),
          height: $(element[0].querySelector('[id^="node-"]'))
            .outerHeight(true)
        };

        unselectNodes();

        element.addClass('has-cursor-crosshair');

        $selectionElement.css({
          'top': startPoint.y,
          'left': startPoint.x
        });

        $selectionElement.stop()
          .fadeIn(200);

        $document.on('mousemove', paint);

        event.preventDefault();
      };

      var endPainting = function endPainting(event) {
        element.removeClass('has-cursor-crosshair');

        $selectionElement.fadeOut(100, () => {
          $selectionElement.css({
            'left': 0,
            'top': 0,
            'width': 0,
            'height': 0
          });
        });

        // TODO is it leak of memory?
        workflowNodes = null;

        $document.off('mousemove', paint);
      };

      var paint = function paint(event) {
        currentPoint = MouseEvent.getEventOffsetOfElement(event, element[0]);

        var diff = {
          x: currentPoint.x - startPoint.x,
          y: currentPoint.y - startPoint.y
        };

        var selectionElementDimensions = {
          width: diff.x,
          height: diff.y
        };

        $selectionElement.css({
          'width': Math.abs(selectionElementDimensions.width),
          'height': Math.abs(selectionElementDimensions.height)
        });

        calculate('x', diff);
        calculate('y', diff);
        selectNodes(selectionElementDimensions);

        // fix for borders disappearing after paint finish
        viewFix();
      };

      var calculate = function calculate(axis, diff) {
        if (diff[axis] < 0) {
          $selectionElement.css(axisToWord[axis].original, 'auto');
          $selectionElement.css(axisToWord[axis].opposite,
            elementDimensions[axisToWord[axis].dimension] - startPoint[axis]);
        } else {
          $selectionElement.css(axisToWord[axis].original, startPoint[axis]);
          $selectionElement.css(axisToWord[axis].opposite, 'auto');
        }
      };

      let intersect = (A, B) => {
        return A.left <= B.right &&
          A.top <= B.bottom &&
          B.top <= A.bottom &&
          B.left <= A.right;
      };

      var selectNodes = function selectNodes(selectionElementDimensions) {
        unselectNodes();
        _.each(_.filter(workflowNodes, (node) => {
          return intersect({
            left: node.x,
            top: node.y,
            right: node.x + nodeDimensions.width,
            bottom: node.y + nodeDimensions.height
          }, {
            left: Math.min(startPoint.x, startPoint.x + selectionElementDimensions.width),
            top: Math.min(startPoint.y, startPoint.y + selectionElementDimensions.height),
            right: Math.max(startPoint.x, startPoint.x + selectionElementDimensions.width),
            bottom: Math.max(startPoint.y, startPoint.y + selectionElementDimensions.height)
          });
        }), (node) => {
          addToSelection(node);
        });
      };

      var unselectNodes = function unselectNodes() {
        _.each(
          inSelection,
          DOMNode => DOMNode.classList.remove('flowchart-node--active')
        );

        jsPlumb.clearDragSelection();
        inSelection.length = 0;
      };

      var addToSelection = function addToSelection(node) {
        var DOMNode = $document[0].getElementById(`node-${node.id}`);

        if (inSelection.indexOf(DOMNode) === -1) {
          inSelection.push(DOMNode);
          DOMNode.classList.add('flowchart-node--active');
          jsPlumb.addToDragSelection(DOMNode);
        }
      };

      var viewFix = debounce(function viewFix() {
        let oldRepaintValue = $selectionElement[0].style.left;
        let newRepaintValue = parseInt($selectionElement[0].style.left) - 1;

        // force to repaint
        $selectionElement[0].style.left =
          newRepaintValue ? `${newRepaintValue}px` : 'auto';

        $timeout(() => {
          // return value before forcing
          $selectionElement[0].style.left = oldRepaintValue;
        }, false);
      }, 50, true);

      var init = function init() {
        // put selection element
        selectionElement.className = CLASS_NAME;
        selectionElement.style.display = 'none';
        element.append(selectionElement);
      };

      element.on('mousedown', startPainting);
      $document.on('mouseup', endPainting);

      scope.$on(GraphNode.MOUSEDOWN, (e, data) => {
        if (
          _.find(
            inSelection,
            DOMNode => DOMNode.id === `node-${data.selectedNode.id}`
          ) === undefined
        ) {
          unselectNodes();
        }
      });

      scope.$on(GraphNode.MOVE, () => {
        $rootScope.$broadcast('MultipleSelection.STOP_DRAG');
      });

      scope.$on('$destroy', () => {
        $document.off('mouseup', endPainting);
        $document.off('mousemove', paint);
      });

      scope.$on('ZOOM-PANEL.MOVE-GRAB', (e, data) => {
        disabled = data.active;
      });

      init();
    }
  };
}

exports.inject = function(module) {
  module.directive('multiSelection', MultiSelection);
};
