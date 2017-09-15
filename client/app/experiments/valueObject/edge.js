/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

function Edge(options) {
  var that = this;
  that.init = function init() {
    that.startNodeId = options.startNodeId;
    that.endNodeId = options.endNodeId;
    that.startPortId = options.startPortId;
    that.endPortId = options.endPortId;
  };
  that.init();
}

module.exports = Edge;
