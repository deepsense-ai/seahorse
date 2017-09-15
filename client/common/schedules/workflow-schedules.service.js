'use strict';


export default class WorkflowSchedulesService {
  constructor(SchedulingManagerApiService, UUIDGenerator, $log) {
    'ngInject';

    this.$log = $log;

    this.schedulingManagerApi = SchedulingManagerApiService;
    this.uuid = UUIDGenerator;
  }


  deleteSchedule(scheduleId) {
    this.$log.info(`WorkflowSchedulesService.deleteSchedule(${scheduleId})`);

    return this.schedulingManagerApi
      .deleteWorkflowSchedule(scheduleId);
  }


  fetchSchedules(workflowId) {
    this.$log.info(`WorkflowSchedulesService.fetchSchedules(${workflowId})`);

    return this.schedulingManagerApi
      .getSchedulesForWorkflow(workflowId);
  }


  generateScheduleStub(workflowId) {
    this.$log.info(`WorkflowSchedulesService.generateScheduleStub(${workflowId})`);

    return {
      id: this.uuid.generateUUID(),
      schedule: {
        cron: '0 12 * * *'
      },
      workflowId: workflowId,
      executionInfo: {
        emailForReports: '',
        presetId: -1
      }
    };
  }


  updateSchedule(schedule) {
    this.$log.info('WorkflowSchedulesService.updateSchedule()', schedule);

    return this.schedulingManagerApi
      .putWorkflowSchedule(schedule);
  }
}
