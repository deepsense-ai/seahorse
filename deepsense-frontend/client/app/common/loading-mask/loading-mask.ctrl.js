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

  function processDots () {
    runningDots = true;
    lmCtrl.dots = lmCtrl.dots.length >= 3 ? '.' : lmCtrl.dots + '.';
    timer = $timeout(processDots, 450);
  }

}

exports.inject = function(module) {
  module.controller('LoadingMaskCtrl', LoadingMaskCtrl);
};
