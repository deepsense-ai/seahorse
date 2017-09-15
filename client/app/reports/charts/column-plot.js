/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

function ColumnPlot() {
  return {
    restrict: 'E',
    templateUrl: 'app/reports/charts/plot.html',
    replace: true,
    scope: {
      'data': '='
    },
    link: function (scope, element) {
      function displayChart (data) {
        $(element).highcharts({
          chart: {
            type: 'column'
          },
          title: null,
          subtitle: null,
          xAxis: {
            categories: data.buckets,
            labels: {
              rotation: -45,
              style: {
                fontSize: '13px',
                fontFamily: 'Verdana, sans-serif'
              }
            }
          },
          yAxis: {
            min: 0,
            title: {
              text: ''
            }
          },
          legend: {
            enabled: false
          },
          tooltip: {
            pointFormat: 'The value: <b>{point.y}</b>'
          },
          series: [{
            data: data.counts
          }]
        });
      }

      scope.$applyAsync(() => {
        scope.$watch('data', displayChart);
      });
    }
  };
}

exports.inject = function (module) {
  module.directive('columnPlot', ColumnPlot);
};
