'use strict';

exports.inject = function(module) {
  require('./workflows-editor.controller.js').inject(module);
  require('./workflows-editor.config.js').inject(module);
  require('./graph-nodes.service.js').inject(module);
};
