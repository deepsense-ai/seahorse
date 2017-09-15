'use strict';

/*@ngInject*/
function InputTextChangeListener($rootScope) {
  return {
    restrict: 'A',
    link: function (scope, element) {
      let $el = $(element);
      let currValue = $el.val();
      let triggerChangeEvent = () => {
        $rootScope.$applyAsync(() => {
          $rootScope.$broadcast('AttributesPanel.UPDATED');
        });
      };
      let compareValues = () => {
        if ($el.val() !== currValue) {
          currValue = $el.val();
          triggerChangeEvent(currValue);
        }
      };

      $el.on('focus', () => {
        currValue = $el.val();
      });

      $el.on('blur', () => {
        compareValues();
      });
    }
  };
}

angular.module('deepsense.attributes-panel').
  directive('inputTextChangeListener', InputTextChangeListener);
