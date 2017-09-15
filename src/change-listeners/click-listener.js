'use strict';

/*@ngInject*/
function ClickListener($rootScope) {
  return {
    restrict: 'A',
    link: function (scope, element) {
      let $el = $(element);
      let triggerChangeEvent = () => {
        $rootScope.$applyAsync(() => {
          $rootScope.$broadcast('AttributesPanel.UPDATED');
        });
      };

      $el.on('click', () => {
        triggerChangeEvent();
      });
    }
  };
}

angular.module('deepsense.attributes-panel').
  directive('clickListener', ClickListener);
