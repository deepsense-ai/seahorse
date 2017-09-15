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
