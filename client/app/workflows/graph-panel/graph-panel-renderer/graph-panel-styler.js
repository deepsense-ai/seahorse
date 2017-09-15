'use strict';

import { GraphPanelRendererBase } from './graph-panel-renderer-base.js';

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
    endpoint.setPaintStyle({
      fillStyle: '#216477'
    });
  }

  static styleInputEndpointTypeMatch(endpoint) {
    endpoint.setPaintStyle({
      fillStyle: '#1ab394'
    });
  }

  static styleInputEndpointTypeDismatch(endpoint) {
    endpoint.setPaintStyle({
      fillStyle: '#ed5565'
    });
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

  static getInputEndpointDefaultCssClass(renderMode) {
    return renderMode === GraphPanelRendererBase.EDITOR_RENDER_MODE ?
      'input-endpoint-default-style-editor-mode' :
      'input-endpoint-default-style-report-mode fa fa-bar-chart';
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

  static getInputEndpointDefaultHoverCssClass(renderMode) {
    return renderMode === GraphPanelRendererBase.EDITOR_RENDER_MODE ?
      'input-endpoint-default-hover-style-editor-mode' :
      'input-endpoint-default-hover-style-report-mode';
  }

  static getOutputEndpointDefaultPaintStyle(renderMode, hasReport) {
    if (renderMode === GraphPanelRendererBase.EDITOR_RENDER_MODE) {
      return {
        fillStyle: ENDPOINT_DEFAULT_BACKGROUND_COLOR,
        radius: 10,
        lineWidth: 2
      };
    } else {
      return {
        fillStyle: ENDPOINT_DEFAULT_BACKGROUND_COLOR,
        radius: hasReport ? 15 : 10,
        lineWidth: 2
      };
    }
  }

  static getOutputEndpointDefaultCssClass(renderMode, hasReport) {
    if (renderMode === GraphPanelRendererBase.EDITOR_RENDER_MODE) {
      return 'output-endpoint-default-style-editor-mode';
    } else {
      return 'output-endpoint-default-style-report-mode' + (hasReport ? ' fa fa-bar-chart' : '');
    }
  }

  static getOutputEndpointDefaultHoverPaintStyle(renderMode, hasReport) {
    return renderMode === GraphPanelRendererBase.EDITOR_RENDER_MODE ? {
      fillStyle: '#216477',
      radius: 10,
      lineWidth: 2
    } : {
      fillStyle: ENDPOINT_DEFAULT_BACKGROUND_COLOR,
      radius: hasReport ? 20 : 10,
      lineWidth: 2
    };
  }

  static getOutputEndpointDefaultHoverCssClass(renderMode, hasReport) {
    if (renderMode === GraphPanelRendererBase.EDITOR_RENDER_MODE) {
      return 'output-endpoint-default-hover-style-editor-mode';
    } else {
      return hasReport ? 'output-endpoint-default-hover-style-report-mode' : '';
    }
  }
}
