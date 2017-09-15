'use strict';

exports.inject = function(module) {
  require('./new-workflow-modal/new-workflow-modal.ctrl.js')
    .inject(module);
  require('./upload-workflow-modal/upload-workflow-modal.ctrl.js')
    .inject(module);
  require('./upload-execution-report-modal/upload-execution-report-modal.ctrl.js')
    .inject(module);
  require('./confirmation-modal/confirmation-modal.ctrl.js')
    .inject(module);
  require('./confirmation-modal/confirmation-modal.service.js')
    .inject(module);
  require('./export-modal/export-modal.ctrl.js')
    .inject(module);
  require('./export-modal/export-modal.service.js')
    .inject(module);
};
