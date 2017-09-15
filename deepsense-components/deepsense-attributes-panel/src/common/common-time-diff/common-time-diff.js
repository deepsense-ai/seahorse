angular.module('deepsense.attributes-panel')
  .directive('timeDiff', function ($timeout) {
    return {
      restrict: 'E',
      templateUrl: 'common/common-time-diff/common-time-diff.html',
      scope: {
        start: '=',
        end: '='
      },
      controller: function () {
        this.getDiff = () => {
          return (new Date(this.end) - new Date(this.start)) / 1000;
        };
      },
      controllerAs: 'controller',
      bindToController: true
    };
  });