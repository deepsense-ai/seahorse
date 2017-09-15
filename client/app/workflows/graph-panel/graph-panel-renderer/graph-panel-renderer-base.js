'use strict';

export class GraphPanelRendererBase {
  constructor() {
  }

  getNodeById(id) {
    return document.querySelector(`#node-${id}`);
  }
}

GraphPanelRendererBase.EDITOR_RENDER_MODE = Symbol('editor_render_mode');
GraphPanelRendererBase.REPORT_RENDER_MODE = Symbol('report_render_mode');
