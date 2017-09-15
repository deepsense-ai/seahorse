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

import jsPlumb from 'jsplumb';

let internal = {};
class MultiSelection {
  constructor($document, $timeout, $rootScope, MouseEvent, MultiSelectionService, debounce) {
    internal = {
      $document, $timeout, $rootScope, MouseEvent, MultiSelectionService, debounce
    };
    this.restrict = 'A';
  }

  // TODO Remove jshint ignore and refactor code.
  /* jshint ignore:start */
  link(scope, element) {
      let that = {};

      const CLASS_NAME = 'selection-element';
      const CTRL_KEY = true;
      let selectionElement = internal.$document[0].createElement('div');
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
      let workflowNodes;
      let disabled;

      that.intersect = (A, B) => {
        return A.left <= B.right &&
          A.top <= B.bottom &&
          B.top <= A.bottom &&
          B.left <= A.right;
      };

      that._getWorkflowFromInheritedScope = () => {
        return scope.$ctrl.workflow;
      };

      that.startPainting = (event) => {
        if (event.target === element[0]) {
          event.preventDefault();
        }

        internal.$document.on('mousemove', that.paint);
        internal.$document.on('mouseup', that.endPainting);

        if (disabled || event.button !== 0) {
          return;
        }

        startPoint = internal.MouseEvent.getEventOffsetOfElement(event, element[0].parentElement);

        workflowNodes = workflowNodes || _.map(that._getWorkflowFromInheritedScope().getNodes(),
          node => {
            return {
              x: node.x,
              y: node.y,
              height: $(element[0].querySelector('#node-' + node.id)).outerHeight(true),
              width: $(element[0].querySelector('#node-' + node.id)).outerWidth(true),
              id: node.id
            };
          }
        );

        elementDimensions = elementDimensions || {
          width: element[0].clientWidth,
          height: element[0].clientHeight
        };

        element.addClass('has-cursor-crosshair');
        $selectionElement.css({
          'top': startPoint.y,
          'left': startPoint.x
        });

        $selectionElement.stop().fadeIn(200);
      };

      that.endPainting = () => {
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
        internal.$document.off('mousemove', that.paint);
        internal.$document.off('mouseup', that.endPainting);
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

      that.selectNodes = (selectionElementDimensions) => {
        that.clearAllFromSelection();
        that.filterNodes(selectionElementDimensions);
      };

      that.filterNodes = (selectionElementDimensions) => {
        let interceptedNodes = _.filter(workflowNodes, (node) => {
          return that.intersect({
            left: node.x,
            top: node.y,
            right: node.x + node.width,
            bottom: node.y + node.height
          }, {
            left: Math.min(startPoint.x, startPoint.x + selectionElementDimensions.width),
            top: Math.min(startPoint.y, startPoint.y + selectionElementDimensions.height),
            right: Math.max(startPoint.x, startPoint.x + selectionElementDimensions.width),
            bottom: Math.max(startPoint.y, startPoint.y + selectionElementDimensions.height)
          });
        });
        _.each(interceptedNodes, (node) => {
          internal.MultiSelectionService.addNodeIdsToSelection([node.id]);
          that.addToSelection([node.id]);
        });
      };

      that.viewFix = internal.debounce(50, () => {
        let oldRepaintValue = $selectionElement[0].style.left;
        let newRepaintValue = parseInt($selectionElement[0].style.left, 10) - 1;

        // force to repaint
        $selectionElement[0].style.left =
          newRepaintValue ? `${newRepaintValue}px` : 'auto';

        internal.$timeout(() => {
          // return value before forcing
          $selectionElement[0].style.left = oldRepaintValue;
        }, false);
      }, true);

      that.paint = (event) => {
        let currentPoint = internal.MouseEvent.getEventOffsetOfElement(event, element[0].parentElement);
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

        if (internal.MouseEvent.isModKeyDown(event)) {
          that.filterNodes(selectionElementDimensions, CTRL_KEY);
        } else {
          that.selectNodes(selectionElementDimensions);
        }

        that.viewFix();
      };

      that.addToSelection = (nodeIDs) => {
        let DOMNodes = _.map(nodeIDs, (nodeId) => {
          let DOMNode = that.findDOMNodeById(nodeId);
          DOMNode.classList.add('graph-node--active');
          jsPlumb.addToDragSelection(DOMNode);
          return DOMNode;
        });
        internal.MultiSelectionService.addNodeIdsToSelection(nodeIDs);
        inSelection = _.union(inSelection, DOMNodes);
      };

      that.removeFromSelection = (nodeIDs) => {
        let DOMNodes = _.map(nodeIDs, (nodeId) => {
          let DOMNode = that.findDOMNodeById(nodeId);
          DOMNode.classList.remove('graph-node--active');
          jsPlumb.removeFromDragSelection(DOMNode);
          return DOMNode;
        });
        internal.MultiSelectionService.removeNodeIdsFromSelection(nodeIDs);
        inSelection = _.difference(inSelection, DOMNodes);
      };

      that.clearAllFromSelection = () => {
        _.each(inSelection, (DOMNode) => {
          DOMNode.classList.remove('graph-node--active');
        });
        internal.MultiSelectionService.clearSelection();
        jsPlumb.clearDragSelection();
        inSelection = [];
      };

      that.fixNodeId = (nodeId) => {
        return nodeId.slice(5);
      };

      that.findDOMNodeById = (nodeId) => {
        return internal.$document[0].getElementById(`node-${nodeId}`);
      };

      that.clearNodes = (event) => {
        if (!internal.MouseEvent.isModKeyDown(event)) {
          internal.MultiSelectionService.clearSelection();
          that.clearAllFromSelection();
          that.viewFix();
        }
      };

      that.graphNodeMouseDownHandler = (event, data) => {
        if (internal.MouseEvent.isModKeyDown(data.originalEvent)) {
          if (internal.MultiSelectionService.isAlreadyAddedToSelection(data.selectedNode)) {
            internal.MultiSelectionService.removeNodeIdsFromSelection([data.selectedNode.id]);
            that.removeFromSelection([data.selectedNode.id]);
          } else {
            internal.MultiSelectionService.addNodeIdsToSelection([data.selectedNode.id]);
            that.addToSelection([data.selectedNode.id]);
          }
        } else {
          if (!internal.MultiSelectionService.isAlreadyAddedToSelection(data.selectedNode)) {
            internal.MultiSelectionService.clearSelection();
            that.clearAllFromSelection();
          }
          internal.MultiSelectionService.addNodeIdsToSelection([data.selectedNode.id]);
          that.addToSelection([data.selectedNode.id]);
        }
        that.endPainting();
      };

      that.graphNodeMouseUpHandler = (event, data) => {
        internal.$rootScope.$broadcast('MultipleSelection.STOP_DRAG');
      };

      that.unregisterNodeDown = scope.$on('GraphNode.MOUSEDOWN', that.graphNodeMouseDownHandler);
      that.unregisterNodeUp = scope.$on('GraphNode.MOUSEUP', that.graphNodeMouseUpHandler);

      scope.$on('$destroy', () => {
        internal.$document.off('mouseup', that.endPainting);
        internal.$document.off('mouseup', that.paint);
      });

      scope.$on('INTERACTION-PANEL.MOVE-GRAB', (e, data) => {
        disabled = data.active;
      });

      that.init = () => {
        selectionElement.className = CLASS_NAME;
        selectionElement.style.display = 'none';
        element.append(selectionElement);
        element.on('mousedown', that.startPainting);
        element.on('mousedown', that.clearNodes);

        scope.$on('MultiSelection.ADD', (e, nodeIds) => {
          that.addToSelection(nodeIds);
        });

        scope.$on('MultiSelection.CLEAR_ALL', (event) => {
          that.clearAllFromSelection();
        });
      };

      that.init();
    }
    /* jshint ignore:end */

  /* @ngInject */
  static directiveFactory($document, $timeout, $rootScope, MouseEvent, MultiSelectionService, debounce) {
    MultiSelection.instance = new MultiSelection($document, $timeout, $rootScope, MouseEvent, MultiSelectionService, debounce);
    return MultiSelection.instance;
  }
}
MultiSelection.directiveFactory.$inject = ['$document', '$timeout', '$rootScope', 'MouseEvent', 'MultiSelectionService', 'debounce'];

exports.inject = function(module) {
  module.directive('multiSelection', MultiSelection.directiveFactory);
};
