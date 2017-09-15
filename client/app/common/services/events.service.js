'use strict';

/* @ngInject */
function EventsBus($rootScope) {
  const service = this;
  service.EVENTS = {
    WORKFLOW_DELETE_SELECTED_ELEMENT: 'Workflow.DELETE_SELECTED_ELEMENT'
  };
  service.on = bindListener;
  service.off = removeEventListener;
  service.publish = publish;

  return service;

  function bindListener(eventName, callback) {
    return $rootScope.$on(eventName, callback);
  }

  function removeEventListener(removeFunction) {
    return removeFunction();
  }

  function publish(eventName, params) {
    return $rootScope.$emit(eventName, params);
  }
}

exports.inject = function(module) {
  module.service('EventsService', EventsBus);
};
