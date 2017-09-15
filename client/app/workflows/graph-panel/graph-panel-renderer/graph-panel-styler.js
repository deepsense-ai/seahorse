'use strict';

import { GraphPanelRendererBase } from './graph-panel-renderer-base.js';

const ENDPOINT_DEFAULT_BACKGROUND_COLOR = '#00b1eb';

export class GraphPanelStyler {
  static styleInputEndpointDefault(endpoint, renderMode) {
    endpoint.setPaintStyle(GraphPanelStyler.getInputEndpointDefaultPaintStyle(renderMode));
    endpoint.setHoverPaintStyle(GraphPanelStyler.getInputEndpointDefaultHoverPaintStyle(renderMode));
  }

  static styleOutputEndpointDefault(endpoint, renderMode) {
    endpoint.setPaintStyle(GraphPanelStyler.getOutputEndpointDefaultPaintStyle(renderMode));
    endpoint.setHoverPaintStyle(GraphPanelStyler.getOutputEndpointDefaultHoverPaintStyle(renderMode));
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

  static getOutputEndpointDefaultPaintStyle(renderMode) {
    return renderMode === GraphPanelRendererBase.EDITOR_RENDER_MODE ? {
      fillStyle: ENDPOINT_DEFAULT_BACKGROUND_COLOR,
      radius: 10,
      lineWidth: 2
    } : {
      fillStyle: ENDPOINT_DEFAULT_BACKGROUND_COLOR,
      radius: 15,
      lineWidth: 2
    };
  }

  static getOutputEndpointDefaultCssClass(renderMode) {
    return renderMode === GraphPanelRendererBase.EDITOR_RENDER_MODE ?
      'output-endpoint-default-style-editor-mode' :
      'output-endpoint-default-style-report-mode fa fa-bar-chart';
  }

  static getOutputEndpointDefaultHoverPaintStyle(renderMode) {
    return renderMode === GraphPanelRendererBase.EDITOR_RENDER_MODE ? {
      fillStyle: '#216477',
      radius: 10,
      lineWidth: 2
    } : {
      fillStyle: ENDPOINT_DEFAULT_BACKGROUND_COLOR,
      radius: 20,
      lineWidth: 2
    };
  }

  static getOutputEndpointDefaultHoverCssClass(renderMode) {
    return renderMode === GraphPanelRendererBase.EDITOR_RENDER_MODE ?
      'output-endpoint-default-hover-style-editor-mode' :
      'output-endpoint-default-hover-style-report-mode';
  }
}
