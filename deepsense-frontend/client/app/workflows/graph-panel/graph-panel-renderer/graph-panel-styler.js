'use strict';

/* beautify preserve:start */
import { GraphPanelRendererBase } from './graph-panel-renderer-base.js';
/* beautify preserve:end */

const ENDPOINT_DEFAULT_BACKGROUND_COLOR = '#00b1eb';

export class GraphPanelStyler {

  static styleInputEndpointDefault(endpoint, renderMode) {
    endpoint.setPaintStyle(GraphPanelStyler._getInputEndpointDefaultPaintStyle());
    endpoint.setHoverPaintStyle(GraphPanelStyler.getInputEndpointDefaultHoverPaintStyle(renderMode));
  }

  static styleOutputEndpointDefault(endpoint, hasReport) {
    endpoint.setPaintStyle(GraphPanelStyler._getOutputEndpointDefaultPaintStyle(hasReport));
    endpoint.setHoverPaintStyle(GraphPanelStyler._getOutputEndpointDefaultHoverPaintStyle(hasReport));
  }

  static styleSelectedOutputEndpoint(endpoint) {
    let paintStyles = endpoint.getPaintStyle();

    endpoint.setPaintStyle(_.assign(paintStyles, {
      fillStyle: '#216477'
    }));
  }

  static styleInputEndpointTypeMatch(endpoint) {
    let paintStyles = endpoint.getPaintStyle();

    endpoint.setPaintStyle(_.assign(paintStyles, {
      fillStyle: '#1ab394'
    }));
  }

  static styleInputEndpointTypeDismatch(endpoint) {
    let paintStyles = endpoint.getPaintStyle();

    endpoint.setPaintStyle(_.assign(paintStyles, {
      fillStyle: '#ed5565'
    }));
  }

  static _getInputEndpointDefaultPaintStyle() {
    return {
      fillStyle: ENDPOINT_DEFAULT_BACKGROUND_COLOR,
      width: 20,
      height: 20
    };
  }

  static getInputEndpointDefaultHoverPaintStyle(renderMode) {
    return renderMode === GraphPanelRendererBase.EDITOR_RENDER_MODE ? {
      fillStyle: '#216477',
      width: 20,
      height: 20
    } : {
      fillStyle: ENDPOINT_DEFAULT_BACKGROUND_COLOR,
      width: 20,
      height: 20
    };
  }

  static _getOutputEndpointDefaultPaintStyle(hasReport) {
    if (!hasReport) {
      return {
        fillStyle: ENDPOINT_DEFAULT_BACKGROUND_COLOR,
        radius: 10,
        lineWidth: 2
      };
    } else {
      return {
        fillStyle: ENDPOINT_DEFAULT_BACKGROUND_COLOR,
        radius: 15,
        lineWidth: 2
      };
    }
  }

  static getOutputEndpointCssClassForReport() {
    return 'output-endpoint-default-style-report-mode fa fa-bar-chart';
  }

  static _getOutputEndpointDefaultHoverPaintStyle(hasReport) {
    return !hasReport ? {
      fillStyle: '#216477',
      radius: 10,
      lineWidth: 2
    } : {
      fillStyle: ENDPOINT_DEFAULT_BACKGROUND_COLOR,
      radius: 20,
      lineWidth: 2
    };
  }

  static getOutputEndpointHoverClassForReport() {
    return 'output-endpoint-default-hover-style-report-mode';
  }
}
