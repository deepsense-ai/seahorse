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

/* @ngInject */
function LoadingMaskCtrl($scope, $timeout) {
  let lmCtrl = this;

  lmCtrl.dots = '...';
  lmCtrl.isDisconnected = false;
  let runningDots = false;
  let timer = {};

  $scope.$on('ServerCommunication.CONNECTION_LOST', () => {
    lmCtrl.isDisconnected = true;
    if (!runningDots) {
      processDots();
    }
  });

  $scope.$on('ServerCommunication.CONNECTION_ESTABLISHED', () => {
    lmCtrl.isDisconnected = false;
    runningDots = false;
    $timeout.cancel(lmCtrl.timer);
  });

  function processDots() {
    runningDots = true;
    lmCtrl.dots = lmCtrl.dots.length >= 3 ? '.' : lmCtrl.dots + '.';
    timer = $timeout(processDots, 450);
  }

}

exports.inject = function(module) {
  module.controller('LoadingMaskCtrl', LoadingMaskCtrl);
};
