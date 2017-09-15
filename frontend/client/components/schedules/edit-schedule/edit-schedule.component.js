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
