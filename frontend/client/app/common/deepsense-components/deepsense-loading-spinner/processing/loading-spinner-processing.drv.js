/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

import tpl from './loading-spinner-processing.tpl.html';

class LoadingSpinnerProcessing {
  constructor($timeout) {
    this.restrict = 'E';
    this.replace = true;
    this.transclude = true;
    this.templateUrl = tpl;
    this.scope = {
      bg: '@'
    };
    this.link = function (scope) {
      const LENGTH = 3;
      const SPEED = 450;

      function processDots() {
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
