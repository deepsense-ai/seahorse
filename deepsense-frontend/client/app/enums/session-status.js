'use strict';

exports.inject = function(module) {
  module.constant('SessionStatus', {
    NOT_RUNNING: 'not_running',
    CREATING: 'creating',
    RUNNING: 'running',
    ERROR: 'error'
  });
};
