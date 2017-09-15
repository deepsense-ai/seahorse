'use strict';

exports.inject = function(module) {
  require('./graph-panel-node.js')
    .inject(module);
};
