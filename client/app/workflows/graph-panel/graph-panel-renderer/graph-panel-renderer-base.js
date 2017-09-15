'use strict';

export class GraphPanelRendererBase {
  getNodeById(id) {
    return document.querySelector(`#node-${id}`);
  }
}
