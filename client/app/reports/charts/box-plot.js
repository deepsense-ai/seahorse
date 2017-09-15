/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

function BoxPlot() {
  return {
    restrict: 'E',
    templateUrl: 'app/reports/charts/box-plot.html',
    replace: true,
    scope: false,
    link: function (scope, element) {
      let distObject = scope.distObject;

      $(element).highcharts({
        chart: {
          type: 'boxplot'
        },
        title: {
          text: distObject.name
        },
        subtitle: {
          text: distObject.description
        },
        legend: {
          enabled: false
        },
        xAxis: {
          categories: ['']
        },
        yAxis: {
          title: {
            text: 'Age'
          }
        },
        series: [{
          name: 'Ages',
          data: [
            [
              distObject.statistics.min,
              distObject.statistics.firstQuartile,
              distObject.statistics.median,
              distObject.statistics.thirdQuartile,
              distObject.statistics.max
            ]
          ],
          tooltip: {
            headerFormat: ''
          }
        }]
      });
    }
  };
}

exports.inject = function (module) {
  module.directive('boxPlot', BoxPlot);
};
