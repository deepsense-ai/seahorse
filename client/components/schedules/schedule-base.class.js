'use strict';

// App
import { isEmail } from 'COMMON/helpers/validators';


export class ScheduleBaseClass {
  constructor($scope) {
    this.$scope = $scope;

    this.valid = true;
    this.error = {
      emailForReports: {},
      presetId: {}
    };
    this.model = {
      id: '',
      schedule: {
        cron: ''
      },
      executionInfo: {
        emailForReports: '',
        presetId: -1
      }
    };
  }


  $onInit() {
    this.$scope.$watch(
      () => this.model,
      (newValue, oldValue) => {
        if (!angular.equals(newValue, oldValue)) {
          this.validate();
        }
      },
      true
    );
  }


  validate() {
    this.error.presetId.required = !_.find(this.clusterPresets, {
      id: this.model.executionInfo.presetId
    });
    this.error.emailForReports.required = !this.model.executionInfo.emailForReports;
    this.error.emailForReports.email = !isEmail(this.model.executionInfo.emailForReports);

    this.valid = !(
      this.error.presetId.required ||
      this.error.emailForReports.required ||
      this.error.emailForReports.email
    );
  }
}
