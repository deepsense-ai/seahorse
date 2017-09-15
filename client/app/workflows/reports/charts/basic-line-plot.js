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
