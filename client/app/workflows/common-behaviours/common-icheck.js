'use strict';

/* @ngInject */
function Icheck($timeout) {
  return {
    restrict: 'A',
    require: 'ngModel',
    link: function($scope, element, $attrs, ngModel) {
      return $timeout(function() {
        let value;

        value = $attrs.value;

        $scope.$watch($attrs.ngModel, () => {
          $(element).iCheck('update');
        });

        return $(element).
          iCheck({
            checkboxClass: 'icheckbox_square-green',
            radioClass: 'iradio_square-green'
          }).
          on('ifChanged', (event) => {
            if ($(element).attr('type') === 'checkbox' && $attrs.ngModel) {
              $scope.$apply(() => ngModel.$setViewValue(event.target.checked));
            }

            if ($(element).attr('type') === 'radio' && $attrs.ngModel) {
              return $scope.$apply(() => ngModel.$setViewValue(value));
            }
          });
      });
    }
  };
}

exports.inject = function (module) {
  module.directive('icheck', Icheck);
};
