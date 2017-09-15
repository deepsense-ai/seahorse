/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by Oleksandr Tserkovnyi on 24.06.15.
 */
'use strict';

import tpl from './loading-spinner-processing.tpl.html';

class LoadingSpinnerProcessing {
  constructor ($timeout) {
    this.restrict = 'E';
    this.replace = true;
    this.transclude = true;
    this.templateUrl = tpl;
    this.scope = {
      bg: '@'
    };
    this.link = function (scope) {
      const LENGTH  = 3;
      const SPEED   = 450;

      function processDots () {
        scope.dots = scope.dots.length >= LENGTH ? '.' : scope.dots + '.';
        $timeout(processDots, SPEED);
      }

      // Important! If you change this than change .less file "& > span" margin-right property
      scope.dots = '...';

      processDots();
    };
  }

  static factory($timeout) {
    LoadingSpinnerProcessing.instance = new LoadingSpinnerProcessing($timeout);
    return LoadingSpinnerProcessing.instance;
  }
}

LoadingSpinnerProcessing.factory.$inject = ['$timeout'];

angular.module('deepsense.spinner').directive('deepsenseLoadingSpinnerProcessing', LoadingSpinnerProcessing.factory);
