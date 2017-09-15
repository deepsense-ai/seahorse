'use strict';

// Libs
import angular from 'angular';
import _ from 'lodash';

// Assets
import './schedule.less';
import templateUrl from './schedule.html';

// App
import { ScheduleBaseClass } from '../schedule-base.class';


export const ScheduleComponent = {
  templateUrl,

  bindings: {
    clusterPresets: '<',
    initialData: '<',
    onDelete: '&',
    onUpdate: '&'
  },

  controller: class ScheduleController extends ScheduleBaseClass {
    constructor($scope, WorkflowSchedulesService, EventEmitter, $log) {
      'ngInject';

      super($scope);

      this.EventEmitter = EventEmitter;
      this.workflowSchedules = WorkflowSchedulesService;
      this.$log = $log;

      this.jqCronSettings = {
        disable: true
      };
      this.editing = false;
    }


    $onChanges(changed) {
      if (changed.initialData) {
        this.updateModel(this.initialData);
        this.updatePreviewModel();
      }
      if (changed.clusterPresets) {
        this.updatePreviewModel();
      }
    }


    cancelEdit() {
      this.editing = false;
    }


    deleteSchedule() {
      this.workflowSchedules
        .deleteSchedule(this.model.id)
        .then(() => {
          this.onDelete();
        });
    }


    editSchedule() {
      this.editing = true;
    }


    updateSchedule({ schedule }) {
      this.$log.info('ScheduleController.updateSchedule()', schedule);

      this.workflowSchedules
        .updateSchedule(schedule)
        .then((updatedSchedule) => {
          this.updateModel(updatedSchedule);
          this.updatePreviewModel();
          this.editing = false;
          this.onUpdate(
            this.EventEmitter({
              schedule: updatedSchedule
            })
          );
        }, () => {
          this.cancelEdit();
        });
    }


    updateModel(data) {
      this.model = angular.copy(data);
    }


    updatePreviewModel() {
      let preset = _.find(this.clusterPresets, {id: this.model.executionInfo.presetId}) || {name: ''};

      this.model.executionInfo.presetName = preset.name;
    }
  }
};
