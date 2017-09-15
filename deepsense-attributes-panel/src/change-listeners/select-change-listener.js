'use strict';

/*@ngInject*/
function SelectChangeListener($rootScope) {
  return {
    restrict: 'A',
    link: function (scope, element) {
      let $el = $(element);
      let triggerChangeEvent = () => {
        $rootScope.$applyAsync(() => {
          $rootScope.$broadcast('AttributesPanel.UPDATED');
        });
      };

      $el.on('change', () => {
        triggerChangeEvent($el.val());
      });
    }
  };
}

angular.module('deepsense.attributes-panel').
  directive('selectChangeListener', SelectChangeListener);
