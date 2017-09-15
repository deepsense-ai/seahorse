'use strict';

const DEFAULT_COLOR = '#00b1eb';
const HOVER_COLOR = '#028DBB';
const SELECTED_COLOR = '#2f4050';

const MATCHED_GREEN = '#26F323';
const DISMATCHED_RED = '#ed5565';

/* @ngInject */
function GraphPanelStyler() {
  return {
    getTypes: () => {
      return {
        'selected': {
          paintStyle: {
            fillStyle: SELECTED_COLOR
          },
          hoverPaintStyle: {
            fillStyle: SELECTED_COLOR
          },
          endpointStyle: { // Those are needed for dragging to work
            fillStyle: SELECTED_COLOR,
            radius: 15,
            lineWidth: 2
          }
        },
        'matched': {
          paintStyle: {
            fillStyle: MATCHED_GREEN
          }
        },
        'dismatched': {
          paintStyle: {
            fillStyle: DISMATCHED_RED
          }
        },
        'input': {
          paintStyle: {
            fillStyle: DEFAULT_COLOR,
            width: 20,
            height: 20
          },
          hoverPaintStyle: {
            fillStyle: HOVER_COLOR
          }
        },
        'outputWithReport': {
          paintStyle: {
            fillStyle: DEFAULT_COLOR,
            radius: 15,
            lineWidth: 2
          },
          hoverPaintStyle: {
            fillStyle: HOVER_COLOR
          },
          cssClass: 'position-endpoint-icon fa fa-bar-chart'
        },
        'outputNoReport': {
          paintStyle: {
            fillStyle: DEFAULT_COLOR,
            radius: 10,
            lineWidth: 2
          },
          hoverPaintStyle: {
            fillStyle: HOVER_COLOR
          }
        }
      };
    },
    styleInputEndpointDefault: (endpoint) => {
      endpoint.addType('input');
    },
    styleOutputEndpointDefault: (endpoint, hasReport) => {
      if (hasReport) {
        endpoint.addType('outputWithReport');
      } else {
        endpoint.addType('outputNoReport');
      }
    },
    styleSelectedOutputEndpoint: (endpoint) => {
      endpoint.addType('selected');
    },
    styleInputEndpointTypeMatch: (endpoint) => {
      endpoint.addType('matched');
    },
    styleInputEndpointTypeDismatch: (endpoint) => {
      endpoint.addType('dismatched');
    }
  };
}

exports.inject = function (module) {
  module.service('GraphPanelStyler', GraphPanelStyler);
};
