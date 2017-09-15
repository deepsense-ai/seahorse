'use strict';

// Libs
import angular from 'angular';

// Assets
import './schedule.less';
import templateUrl from './schedule.html';

// App
import { ScheduleBaseClass } from '../schedule-base.class';


export const ScheduleComponent = {
  templateUrl,

  bindings: {
    clusterPresets: '<',
    onDelete: '&',
    onUpdate: '&',
    scheduleData: '<'
  },

  controller: class Schedule extends ScheduleBaseClass {
    constructor($scope, WorkflowSchedulesService, EventEmitter) {
      'ngInject';

      super($scope);

      this.workflowSchedules = WorkflowSchedulesService;
      this.EventEmitter = EventEmitter;

      this.jqCronSettings = {};
      this.editing = {};
    }


    $onChanges(changed) {
      if (changed.scheduleData) {
        this.model = angular.copy(this.scheduleData);
        this.validate();
      }
      if (changed.clusterPresets) {
        this.validate();
      }
    }


    cancelEditEmailForReports() {
      this.editing.emailForReports = false;
      this.model.executionInfo.emailForReports = this.scheduleData.executionInfo.emailForReports;
    }


    deleteSchedule() {
      this.workflowSchedules
        .deleteSchedule(this.model.id)
        .then(() => {
          this.onDelete();
        });
    }


    finishEditEmailForReports() {
      this.editing.emailForReports = false;
    }


    startEditEmailForReports() {
      this.editing.emailForReports = true;
    }


    update() {
      if (!this.valid) {
        return;
      }

      this.workflowSchedules
        .updateSchedule(this.model)
        .then((schedule) => {
          this.onUpdate(
            this.EventEmitter({
              schedule: schedule
            })
          );
        });
    }
  }
};
