'use strict';

// Assets
import './edit-schedule.less';
import templateUrl from './edit-schedule.html';

// App
import { ScheduleBaseClass } from '../schedule-base.class';


export const EditScheduleComponent = {
  templateUrl,

  bindings: {
    clusterPresets: '<',
    initialData: '<',
    onAccept: '&',
    onCancel: '&'
  },

  controller: class EditScheduleController extends ScheduleBaseClass {
    constructor($scope, EventEmitter, $log) {
      'ngInject';

      super($scope);

      this.EventEmitter = EventEmitter;
      this.$log = $log;

      this.jqCronSettings = {};
    }


    $onChanges(changed) {
      if (changed.initialData) {
        this.updateModel(this.initialData);
      }
      if (changed.clusterPresets) {
        this.validate();
      }
    }


    cancelEdit() {
      this.onCancel();
    }


    updateModel(data) {
      this.model = angular.copy(data);
    }


    updateSchedule() {
      this.$log.info('EditScheduleController.updateSchedule()', this.model);

      this.onAccept(
        this.EventEmitter({
          schedule: this.model
        })
      );
    }
  }
};
