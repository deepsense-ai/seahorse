'use strict';

/* @ngInject */
function MultiSelection(GraphNode, MouseEvent, WorkflowService, MultiSelectionService,
  $document, $timeout, $rootScope,
  debounce) {
  return {
    restrict: 'A',

    link: (scope, element) => {
      let that = {};
      const CLASS_NAME = 'selection-element';
      let selectionElement = $document[0].createElement('div');
      let $selectionElement = $(selectionElement);
      let startPoint = {
        x: 0,
        y: 0
      };
      let elementDimensions;
      let axisToWord = {
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
      let inSelection = [];
      let nodeDimensions;
      let workflowNodes;
      let disabled;

      that.intersect = (A, B) => {
        return A.left <= B.right &&
          A.top <= B.bottom &&
          B.top <= A.bottom &&
          B.left <= A.right;
      };

      that.startPainting = () => {
        $document.on('mousemove', that.paint);
        $document.on('mouseup', that.endPainting);

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
          width: $(element[0].querySelector('[id^="node-"]')).outerWidth(true),
          height: $(element[0].querySelector('[id^="node-"]')).outerHeight(true)
        };

        that.unselectNodes();

        element.addClass('has-cursor-crosshair');

        $selectionElement.css({
          'top': startPoint.y,
          'left': startPoint.x
        });

        $selectionElement.stop().fadeIn(200);
        event.preventDefault();
      };

      that.endPainting = () => {

        MultiSelectionService.setSelectedNodes(_.map(inSelection, (node) => {
          return (node.id).slice(5);
        }));

        element.removeClass('has-cursor-crosshair');

        $selectionElement.fadeOut(100, () => {
          $selectionElement.css({
            'left': 0,
            'top': 0,
            'width': 0,
            'height': 0
          });
        });

        workflowNodes = null;

        $document.off('mousemove', that.paint);
        $document.off('mouseup', that.endPainting);
      };

      that.calculate = (axis, diff) => {
        if (diff[axis] < 0) {
          $selectionElement.css(axisToWord[axis].original, 'auto');
          $selectionElement.css(axisToWord[axis].opposite,
            elementDimensions[axisToWord[axis].dimension] - startPoint[axis]);
        } else {
          $selectionElement.css(axisToWord[axis].original, startPoint[axis]);
          $selectionElement.css(axisToWord[axis].opposite, 'auto');
        }
      };

      that.unselectNodes = () => {
        _.each(
          inSelection,
          DOMNode => DOMNode.classList.remove('flowchart-node--active')
        );

        jsPlumb.clearDragSelection();
        inSelection.length = 0;
      };

      that.addToSelection = (node) => {
        var DOMNode = $document[0].getElementById(`node-${node.id}`);

        if (inSelection.indexOf(DOMNode) === -1) {
          inSelection.push(DOMNode);
          DOMNode.classList.add('flowchart-node--active');
          jsPlumb.addToDragSelection(DOMNode);
        }
      };

      that.selectNodes = (selectionElementDimensions) => {
        that.unselectNodes();
        _.each(_.filter(workflowNodes, (node) => {
          return that.intersect({
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
          that.addToSelection(node);
        });
      };

      that.viewFix = debounce(() => {
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

      that.paint = (event) => {

        let currentPoint;

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

        that.calculate('x', diff);
        that.calculate('y', diff);
        that.selectNodes(selectionElementDimensions);
        that.viewFix();
      };

      scope.$on(GraphNode.MOUSEDOWN, (e, data) => {
        if (_.find(
            inSelection,
            DOMNode => DOMNode.id === `node-${data.selectedNode.id}`) === undefined) {
          that.unselectNodes();
        }
      });

      scope.$on(GraphNode.MOVE, () => {
        $rootScope.$broadcast('MultipleSelection.STOP_DRAG');
      });

      scope.$on('$destroy', () => {
        $document.off('mouseup', that.endPainting);
        $document.off('mouseup', that.paint);
      });

      scope.$on('INTERACTION-PANEL.MOVE-GRAB', (e, data) => {
        disabled = data.active;
      });

      that.init = () => {
        selectionElement.className = CLASS_NAME;
        selectionElement.style.display = 'none';
        element.append(selectionElement);
        element.on('mousedown', that.startPainting);
      };

      that.init();
    }
  };
}

exports.inject = function(module) {
  module.directive('multiSelection', MultiSelection);
};
