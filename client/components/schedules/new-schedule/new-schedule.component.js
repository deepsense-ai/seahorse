'use strict';

// Assets
import './new-schedule.less';
import templateUrl from './new-schedule.html';

// App
import { ScheduleBaseClass } from '../schedule-base.class';


export const NewScheduleComponent = {
  templateUrl,

  bindings: {
    clusterPresets: '<',
    onCancel: '&',
    onCreate: '&',
    workflowId: '<'
  },

  controller: class Schedule extends ScheduleBaseClass {
    constructor($scope, WorkflowSchedulesService) {
      'ngInject';

      super($scope);

      this.workflowSchedules = WorkflowSchedulesService;

      this.jqCronSettings = {};
      this.model = this.workflowSchedules.generateScheduleStub(this.workflowId);
    }


    $onChanges(changed) {
      if (changed.clusterPresets) {
        this.validate();
      }
    }


    addSchedule() {
      this.workflowSchedules
        .updateSchedule(this.model)
        .then(() => {
          this.onCreate();
        });
    }


    cancelAddingSchedule() {
      this.onCancel();
    }
  }
};
