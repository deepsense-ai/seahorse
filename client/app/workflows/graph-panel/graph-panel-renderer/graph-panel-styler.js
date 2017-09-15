'use strict';

const DEFAULT_COLOR = '#00b1eb';
const HOVER_COLOR = '#028DBB';

const MATCHED_GREEN = '#26F323';
const DISMATCHED_RED = '#ed5565';

/* @ngInject */
function GraphPanelStyler() {
  return {
    getTypes: () => {
      return {
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
          cssClass: 'position-endpoint-icon fa fa-bar-chart',
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
  };
}

exports.inject = function (module) {
  module.service('GraphPanelStyler', GraphPanelStyler);
};
