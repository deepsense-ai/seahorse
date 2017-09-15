/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
