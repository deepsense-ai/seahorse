/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by Oleksandr Tserkovnyi on 06.07.15.
 */

angular.module('deepsense.attributes-panel')
  .directive('minValue', function ($timeout) {
    return {
      restrict: 'A',
      require: 'ngModel',
      link: function (scope, elem, attr, controller) {
        var getMin = function getMin () {
          return Number(elem.attr('min')) || Number(attr.minValue) || 0;
        };

        var minValidator = function minValidator(value) {
          var min = getMin();

          controller.$setValidity('minValue', true);

          if (value && value < min) {
            controller.$setViewValue(min.toString());

            $timeout(() => {
              elem.val(min);
            }, 100);

            return min;
          }

          return value;
        };

        controller.$parsers.push(minValidator);
        controller.$formatters.push(minValidator);
      }
    };
});