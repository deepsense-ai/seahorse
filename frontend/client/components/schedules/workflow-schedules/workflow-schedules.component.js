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

// Libs
import _ from 'lodash';

// Assets
import './workflow-schedules.less';
import templateUrl from './workflow-schedules.html';


export const WorkflowSchedulesComponent = {
  templateUrl,

  bindings: {
    workflow: '<'
  },

  controller: class WorkflowSchedules {
    constructor(WorkflowSchedulesService, PresetService, $log, $scope) {
      'ngInject';

      this.$log = $log;

      this.presets = [];
      PresetService
        .fetch()
        .then(() => {
          const presets = PresetService.getAll();
          this.presets = _.map(presets, (preset) => _.pick(preset, ['name', 'id']));
        });

      this.workflowSchedules = WorkflowSchedulesService;
      this.addingSchedule = false;
      this.schedules = [];
      this.getSchedules();

      $scope.$watch(() => PresetService.getAll(), (newValue) => {
        this.presets = newValue;
      });
    }


    addSchedule({ schedule }) {
      this.workflowSchedules
        .updateSchedule(schedule)
        .then(() => {
          this.getSchedules();
        })
        .then(() => {
          this.addingSchedule = false;
        });
    }


    cancelAddingSchedule() {
      this.addingSchedule = false;
    }


    getSchedules() {
      this.workflowSchedules
        .fetchSchedules(this.workflow.id)
        .then((schedules) => {
          this.schedules = schedules;
        });
    }


    toggleAddingSchedule() {
      if (!this.addingSchedule) {
        this.newScheduleStub = this.workflowSchedules.generateScheduleStub(this.workflow.id);
      }
      this.addingSchedule = !this.addingSchedule;
    }


    updateSchedule({ schedule }) {
      this.$log.warn(`WorkflowSchedules.updateSchedule(${schedule.id})`, schedule);

      // The data is up to date, the order is different
      // (updated schedule is the last element of the collection now)
      // Do we need to fetch all schedules / update this schedule?
    }
  }
};
