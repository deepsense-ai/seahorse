'use strict';

export class GraphPanelRendererBase {
  constructor() {}

  getNodeById(id) {
    return document.querySelector(`#node-${id}`);
  }
}

GraphPanelRendererBase.EDITOR_RENDER_MODE = Symbol('editor_render_mode');
GraphPanelRendererBase.RUNNING_RENDER_MODE = Symbol('running_render_mode');
