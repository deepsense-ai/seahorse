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

import tpl from './distribution-continuous-chart.html';

function DistributionContinuousChart() {
  return {
    restrict: 'E',
    templateUrl: tpl,
    replace: true,
    scope: {
      data: '=',
      columnType: '='
    },
    controller: function() {
      _.assign(this, {
        chosenPlot: {
          value: null
        },
        plots: [{
          name: 'Column plot',
          type: 'column'
        }]
      });

      /*
       * The box plot can be displayed on the given data if and only if
       * there have been provided such values like median and quartiles.
       */
      if (this.data && this.data.statistics && this.data.statistics.median) {
        this.plots.push({
          name: 'Box plot',
          type: 'box'
        });
      }

      // default setting
      this.chosenPlot.value = this.plots[0].type;
    },
    controllerAs: 'distributionContinuousChart',
    bindToController: true
  };
}

exports.inject = function(module) {
  module.directive('distributionContinuousChart', DistributionContinuousChart);
};
