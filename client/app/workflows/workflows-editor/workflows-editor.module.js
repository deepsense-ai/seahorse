'use strict';

exports.inject = function (module) {
  require('./graph-panel/graph-panel.module.js').inject(module);
  require('./workflows-editor.controller.js').inject(module);
  require('./workflows-editor.config.js').inject(module);
};
