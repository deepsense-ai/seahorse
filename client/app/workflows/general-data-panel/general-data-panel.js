'use strict';

/* @ngInject */
function GeneralDataPanel() {
  return {
    restrict: 'E',
    templateUrl: 'app/workflows/general-data-panel/general-data-panel.html',
    replace: true,
    scope: {
      'name': '=',
      'description': '=',
      'state': '=',
      'isReportMode': '='
    },
    controller: 'GeneralDataPanelController as controller',
    bindToController: true,
    link: (scope, element) => {
      scope.$applyAsync(() => {
        /*
         * that's the temporary workaround that needs to be removed
         * as soon as there will be a fix in the angular ui bootstrap
         *
         * https://github.com/angular-ui/bootstrap/issues/4458
         */
        let $nodes = $(element)
          .find('.ibox-title__name, .o-panel__description');

        $nodes.hover(
          () => {
            $('body')
              .css('overflow', 'hidden');
          }, () => {
            $('body')
              .css('overflow', 'auto');
          }
        );
      });
    }
  };
}

exports.inject = function(module) {
  module.directive('generalDataPanel', GeneralDataPanel);
};
