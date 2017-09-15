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

/* globals d3, nv */

'use strict';

import tpl from './plot.html';

/* @ngInject */
function PiePlot($filter) {
  const chart = nv.models.pieChart();

  const directive = {
    restrict: 'E',
    templateUrl: tpl,
    replace: true,
    scope: {
      'data': '='
    },
    link: function(scope, element) {
      scope.$watch('data', function(data) {
        displayChart(data, element);
      });

      scope.$on('$destroy', function() {
        chart.tooltip.hidden(true);
      });

      function displayChart(data, element) {
        const chartValues = _.map(data.counts, function (val, idx) {
          val = $filter('precision')(val);
          return {
            x: data.buckets[idx],
            y: val
          };
        });

        chart
            .duration(500)
            .noData('There is no Data to display')
            .labelThreshold(0)
            .labelType('percent');

        chart.tooltip.hideDelay(0);

        //We don't want any formatting to the numbers in tooltip
        chart.tooltip.valueFormatter(n => n);

        d3.select(element[0].querySelector('.svg-plot'))
            .datum(chartValues)
            .call(chart);

        nv.utils.windowResize(chart.update);
      }

    }
  };
  return directive;
}

exports.inject = function(module) {
  module.directive('piePlot', PiePlot);
};
