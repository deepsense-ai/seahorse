'use strict';

exports.inject = function(module) {
  require('./node-status-icon/node-status-icon.js').inject(module);
  require('./plain-node-content/plain-node-content.js').inject(module);
  require('./iconic-node-content/iconic-node-content.js').inject(module);
  require('./source-sink-node-content/source-sink-node-content.js').inject(module);
  require('./graph-panel-node.js').inject(module);
};
