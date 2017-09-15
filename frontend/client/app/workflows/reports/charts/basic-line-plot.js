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

import tpl from './plot.html';

function BasicLinePlot() {
  return {
    restrict: 'E',
    templateUrl: tpl,
    replace: true,
    scope: {
      'data': '='
    },
    link: function(scope, element) {
      function displayChart(data) {
        $(function() {
          $(element)
            .highcharts({
              chart: {
                type: 'spline'
              },
              title: {
                text: ''
              },
              xAxis: {
                reversed: false,
                maxPadding: 0.05,
                showLastLabel: true
              },
              yAxis: {
                title: null
              },
              legend: {
                enabled: false
              },
              tooltip: {
                enabled: false
              },
              plotOptions: {
                spline: {
                  marker: {
                    enable: true
                  }
                }
              },
              series: [{
                data: _.map(data, (point) => _.map(point, parseFloat))
              }]
            });
        });
      }

      scope.$applyAsync(() => {
        scope.$watch('data', displayChart);
      });
    }
  };
}

exports.inject = function(module) {
  module.directive('basicLinePlot', BasicLinePlot);
};
