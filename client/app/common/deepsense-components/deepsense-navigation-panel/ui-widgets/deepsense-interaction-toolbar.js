/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by: Grzegorz Swatowski
 */

'use strict';

import tpl from './deepsense-interaction-toolbar.html';

const DEFAULT_ID = 'unsetID';

/* @ngInject */
function DeepsenseInteractionToolbar($rootScope) {
  return {
    restrict: 'E',
    templateUrl: tpl,
    scope: {
      'zoomId': '@'
    },
    link: (scope) => {
      scope.zoomId = scope.zoomId || DEFAULT_ID;
      scope.$applyAsync(() => {
        scope.handleZoomOutBtnClick = () => {
          $rootScope.$broadcast('INTERACTION-PANEL.ZOOM-OUT', {
            zoomId: scope.zoomId
          });
        };

        scope.handleZoomInBtnClick = () => {
          $rootScope.$broadcast('INTERACTION-PANEL.ZOOM-IN', {
            zoomId: scope.zoomId
          });
        };

        scope.activeMoveBtn = false;
        scope.handleMoveBtnClick = () => {
          scope.activeMoveBtn = !scope.activeMoveBtn;
          $rootScope.$broadcast('INTERACTION-PANEL.MOVE-GRAB', {
            zoomId: scope.zoomId,
            active: scope.activeMoveBtn
          });
        };

        scope.handleFitBtnClick = () => {
          $rootScope.$broadcast('INTERACTION-PANEL.FIT', {
            zoomId: scope.zoomId
          });
        };
      });
    }
  };
}

angular
  .module('deepsense.navigation-panel')
  .directive('deepsenseInteractionToolbar', DeepsenseInteractionToolbar);
