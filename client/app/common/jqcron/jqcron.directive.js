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
