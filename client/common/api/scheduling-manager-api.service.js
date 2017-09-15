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
