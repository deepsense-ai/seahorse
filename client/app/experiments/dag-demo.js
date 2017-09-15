/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

var jsPlumb = require('jsPlumb');

module.exports = function dagDemo() {
  jsPlumb.ready(function () {
    var instance = jsPlumb.getInstance({
      DragOptions: {
        cursor: 'pointer',
        zIndex: 2000
      },
      Container: 'flowchart-box'
    });

    var connectorPaintStyle = {
        lineWidth: 2,
        strokeStyle: '#61B7CF',
        outlineColor: 'white',
        outlineWidth: 2
      },
      connectorHoverStyle = {
        strokeStyle: '#216477',
      },
      endpointHoverStyle = {
        fillStyle: '#216477',
        strokeStyle: '#216477'
      },
      sourceEndpoint = {
        endpoint: 'Dot',
        paintStyle: {
          // strokeStyle: '#7AB02C',
          fillStyle: '#7AB02C',
          radius: 10,
          lineWidth: 2
        },
        isSource: true,
        connector: ['Bezier'],
        connectorStyle: connectorPaintStyle,
        hoverPaintStyle: endpointHoverStyle,
        connectorHoverStyle: connectorHoverStyle,
      },
      targetEndpoint = {
        endpoint: 'Rectangle',
        paintStyle: {
          fillStyle: '#7AB02C'
        },
        hoverPaintStyle: endpointHoverStyle,
        maxConnections: -1,
        dropOptions: {
          hoverClass: 'hover',
          activeClass: 'active'
        },
        isTarget: true
      };

    var addEndpoints = function (toId, sourceAnchors, targetAnchors) {
      for (var i = 0; i < sourceAnchors.length; i++) {
        var sourceUUID = toId + sourceAnchors[i];
        instance.addEndpoint(toId, sourceEndpoint, {
          anchor: sourceAnchors[i],
          uuid: sourceUUID
        });
      }
      for (var j = 0; j < targetAnchors.length; j++) {
        var targetUUID = toId + targetAnchors[j];
        instance.addEndpoint(toId, targetEndpoint, {
          anchor: targetAnchors[j],
          uuid: targetUUID
        });
      }
    };

    instance.doWhileSuspended(function () {
      addEndpoints('flowchart-node-1', ['LeftMiddle', 'BottomCenter'], ['TopCenter', 'RightMiddle']);
      addEndpoints('flowchart-node-2', ['TopCenter', 'BottomCenter'], ['LeftMiddle', 'RightMiddle']);
      addEndpoints('flowchart-node-3', ['RightMiddle', 'BottomCenter'], ['LeftMiddle', 'TopCenter']);

      instance.connect({uuids: ['flowchart-node-1BottomCenter', 'flowchart-node-3TopCenter'], editable: true});
      instance.connect({uuids: ['flowchart-node-1LeftMiddle', 'flowchart-node-2LeftMiddle'], editable: true});
      instance.connect({uuids: ['flowchart-node-2TopCenter', 'flowchart-node-2RightMiddle'], editable: true});
      instance.connect({uuids: ['flowchart-node-3RightMiddle', 'flowchart-node-1RightMiddle'], editable: true});

      instance.draggable(jsPlumb.getSelector('.flowchart-box .flowchart-node'), { grid: [20, 20] });
    });
  });
};
