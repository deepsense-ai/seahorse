/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

import tpl from './reports.html';

function Report($rootScope) {

  return {
    restrict: 'E',
    replace: true,
    templateUrl: tpl,
    controller: 'ReportCtrl as controller',
    bindToController: {
      currentReport: '=report'
    },
    link: (scope, element) => {
      // HACK This is workaround for DS-2724 BUG
      // Explanation:
      // When DOM element gets covered by another DOM element 'mouseout' won't be called.
      // We rely on this event to hide output port tooltip.
      // This is confirmed bug in chrome -> https://code.google.com/p/chromium/issues/detail?id=159389
      //
      // Our scenario: clicking on OUTPUT PORT for node opens report. This report might cover out port
      // thus preventing 'mouseout' event from firing.
      // In our 'bind' function for 'mouseout' in jsPlumbOutputPort we broadcast 'OutputPoint.MOUSEOUT' event.
      // This bevaviour is reproduced here as if output port was mouse-out.
      // NOTES AND RISKS:
      // This hack means that 'OutputPoint.MOUSEOUT' will be called more than once. This is okay as long as
      // all 'OutputPoint.MOUSEOUT' handlers are idempotent. KEEP THEM IDEMPOTENT
      // TODO Remove this when Chrome bug is fixed.
      element.bind('mouseover', (e) => {
        $rootScope.$broadcast('OutputPoint.MOUSEOUT');
      });

      scope.$watch(() => scope.controller.currentReport, () => {
        element[0].scrollTop = 0;
      });
    }
  };
}

exports.inject = function (module) {
  module.directive('report', Report);
};
