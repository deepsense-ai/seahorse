/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

function Edge(options) {

  var that = this;
  that.init = function init() {
    that.startId = options.startId;
    that.endId = options.endId;
  };
  that.init();
}

module.exports = Edge;
