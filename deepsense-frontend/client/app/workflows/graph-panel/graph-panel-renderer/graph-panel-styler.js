'use strict';

/* beautify preserve:start */
import { GraphPanelRendererBase } from './graph-panel-renderer-base.js';
/* beautify preserve:end */

const ENDPOINT_DEFAULT_BACKGROUND_COLOR = '#00b1eb';

export class GraphPanelStyler {

  static styleInputEndpointDefault(endpoint, renderMode) {
    endpoint.setPaintStyle(GraphPanelStyler.getInputEndpointDefaultPaintStyle(renderMode));
    endpoint.setHoverPaintStyle(GraphPanelStyler.getInputEndpointDefaultHoverPaintStyle(renderMode));
  }

  static styleOutputEndpointDefault(endpoint, renderMode, hasReport) {
    endpoint.setPaintStyle(GraphPanelStyler.getOutputEndpointDefaultPaintStyle(renderMode, hasReport));
    endpoint.setHoverPaintStyle(GraphPanelStyler.getOutputEndpointDefaultHoverPaintStyle(renderMode, hasReport));
  }

  static styleSelectedOutputEndpoint(endpoint) {
    let paintStyles = endpoint.getPaintStyle();

    endpoint.setPaintStyle(_.assign(paintStyles, {
      fillStyle: '#216477'
    }));
  }

  static styleInputEndpointTypeMatch(endpoint, hasReport) {
    let paintStyles = endpoint.getPaintStyle();

    endpoint.setPaintStyle(_.assign(paintStyles, {
      fillStyle: '#1ab394'
    }));
  }

  static styleInputEndpointTypeDismatch(endpoint, hasReport) {
    let paintStyles = endpoint.getPaintStyle();

    endpoint.setPaintStyle(_.assign(paintStyles, {
      fillStyle: '#ed5565'
    }));
  }

  static getInputEndpointDefaultPaintStyle(renderMode) {
    return renderMode === GraphPanelRendererBase.EDITOR_RENDER_MODE ? {
      fillStyle: ENDPOINT_DEFAULT_BACKGROUND_COLOR,
      width: 20,
      height: 20
    } : {
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

  static getOutputEndpointDefaultPaintStyle(renderMode, hasReport) {
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

  static getOutputEndpointDefaultCssClass(renderMode, hasReport) {
    if (hasReport) {
      return 'output-endpoint-default-style-report-mode fa fa-bar-chart';
    } else if (renderMode === GraphPanelRendererBase.EDITOR_RENDER_MODE) {
      return 'output-endpoint-default-style-editor-mode';
    }
  }

  static getOutputEndpointDefaultHoverPaintStyle(renderMode, hasReport) {
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

  static getOutputEndpointDefaultHoverCssClass(renderMode, hasReport) {
    if (hasReport) {
      return 'output-endpoint-default-hover-style-report-mode';
    } else if (renderMode === GraphPanelRendererBase.EDITOR_RENDER_MODE) {
      return 'output-endpoint-default-hover-style-editor-mode';
    }
  }
}
