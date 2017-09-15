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


// App
import ApiBaseClass from './api-base.class';


export default class SchedulingManagerApiService extends ApiBaseClass {
  constructor($http, config) {
    'ngInject';

    super($http, config);
    this.servicePath = '/schedulingmanager/v1';
  }


  getSchedulesForWorkflow(workflowId) {
    const endpointUrl = this.makeEndpointUrl(`/workflow/${workflowId}/schedules`);

    return this.$http
      .get(endpointUrl)
      .then(this.getData);
  }


  getWorkflowSchedules() {
    const endpointUrl = this.makeEndpointUrl('/workflow-schedules');

    return this.$http
      .get(endpointUrl)
      .then(this.getData);
  }


  getWorkflowSchedule(scheduleId) {
    const endpointUrl = this.makeEndpointUrl(`/workflow-schedules/${scheduleId}`);

    return this.$http
      .get(endpointUrl)
      .then(this.getData);
  }


  putWorkflowSchedule(workflowSchedule) {
    const endpointUrl = this.makeEndpointUrl(`/workflow-schedules/${workflowSchedule.id}`);

    return this.$http
      .put(endpointUrl, workflowSchedule)
      .then(this.getData);
  }


  deleteWorkflowSchedule(scheduleId) {
    const endpointUrl = this.makeEndpointUrl(`/workflow-schedules/${scheduleId}`);

    return this.$http
      .delete(endpointUrl);
  }
}
