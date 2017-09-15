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
