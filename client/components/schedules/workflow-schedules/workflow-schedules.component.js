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
    constructor(WorkflowSchedulesService, PresetService, $log) {
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
    }


    finishAddingSchedule() {
      this.$log.info('WorkflowSchedules.finishAddingSchedule()');

      this.stopAddingSchedule();
      this.getSchedules();
    }


    getSchedules() {
      this.$log.info('WorkflowSchedules.getSchedules()');

      this.workflowSchedules
        .fetchSchedules(this.workflow.id)
        .then((schedules) => {
          this.schedules = schedules;
          this.$log.info('got ->', this.schedules);
        });
    }


    startAddingSchedule() {
      this.$log.info('WorkflowSchedules.startAddingSchedule()');

      this.addingSchedule = true;
    }


    stopAddingSchedule() {
      this.$log.info('WorkflowSchedules.stopAddingSchedule()');

      this.addingSchedule = false;
    }


    updateSchedule({ schedule }) {
      this.$log.warn(`WorkflowSchedules.updateSchedule(${schedule.id})`, schedule);

      // The data is up to date, the order is different
      // (updated schedule is the last element of the collection now)
      // Do we need to fetch all schedules / update this shedule?
    }
  }
};
