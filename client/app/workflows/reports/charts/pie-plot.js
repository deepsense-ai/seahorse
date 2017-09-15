/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

function PiePlot() {
  return {
    restrict: 'E',
    templateUrl: 'app/workflows/reports/charts/plot.html',
    replace: true,
    scope: {
      'data': '='
    },
    link: function (scope, element) {
      function displayChart (data) {
        $(element).highcharts({
          chart: {
            type: 'pie'
          },
          title: null,
          subtitle: null,
          plotOptions: {
            pie: {
              allowPointSelect: true,
              cursor: 'pointer',
              dataLabels: {
                enabled: true,
                format: '<b>{point.name}</b>: {point.percentage:.1f} %',
                style: {
                  color: (Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black'
                }
              }
            }
          },
          tooltip: {
            headerFormat: 'Value: <b>{point.key}</b><br />',
            pointFormat: 'Occurrence count: <b>{point.y}</b>'
          },
          series: [{
            colorByPoint: true,
            data: _.zip(data.buckets, data.counts)
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
  module.directive('piePlot', PiePlot);
};
