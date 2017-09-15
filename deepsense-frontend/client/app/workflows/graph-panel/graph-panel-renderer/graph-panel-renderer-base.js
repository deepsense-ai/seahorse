'use strict';

export class GraphPanelRendererBase {
  constructor() {}

  getNodeById(id) {
    return document.querySelector(`#node-${id}`);
  }
}
