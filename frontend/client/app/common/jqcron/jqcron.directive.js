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

import 'imports?$=jquery!VENDOR/jqcron/jqCron';
import 'VENDOR/jqcron/jqCron.css';


export default function jqCronDirective() {
  return {
    restrict: 'A',
    require: 'ngModel',
    link: function ($scope, element, attrs, ngModel) {
      if (!ngModel) {
        throw new ReferenceError('jqCronDirective: ngModel is not defined');
      }

      const defaults = {
        lang: 'en',
        /* eslint-disable camelcase */
        enabled_minute: true,
        multiple_dom: true,
        multiple_month: true,
        multiple_mins: true,
        multiple_dow: true,
        multiple_time_hours: true,
        multiple_time_minutes: true,
        numeric_zero_pad: true,
        default_period: 'day',
        default_value: ''
        /* eslint-enable camelcase */
      };

      const options = angular.extend({}, defaults, $scope.$eval(attrs.jqCron));
      const cronInput = $(element).jqCron(options).jqCronGetInstance();


      ngModel.$render = () => {
        cronInput.setCron(ngModel.$viewValue);
      };


      $(element).on('cron:change', (event, value) => {
        $scope.$evalAsync(() => {
          ngModel.$setViewValue(value);
        });
      });
    }
  };
}
