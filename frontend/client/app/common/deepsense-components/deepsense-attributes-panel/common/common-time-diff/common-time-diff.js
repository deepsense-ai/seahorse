'use strict';

import tpl from './common-time-diff.html';

angular.module('deepsense.attributes-panel')
  .directive('timeDiff', function ($timeout) {
    return {
      restrict: 'E',
      templateUrl: tpl,
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
