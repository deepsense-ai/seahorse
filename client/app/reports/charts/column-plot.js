/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

function ColumnPlot() {
  return {
    restrict: 'E',
    templateUrl: 'app/reports/charts/column-plot.html',
    replace: true,
    scope: false,
    link: function (scope, element) {
      let distObject = scope.distObject;

      $(element).highcharts({
        chart: {
          type: 'column'
        },
        title: {
          text: distObject.name
        },
        subtitle: {
          text: distObject.description
        },
        xAxis: {
          categories: distObject.buckets,
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
            text: 'Number of people'
          }
        },
        legend: {
          enabled: false
        },
        tooltip: {
          pointFormat: 'The number of people who got born this year: <b>{point.y}</b>'
        },
        series: [{
          name: 'Population',
          data: distObject.counts
        }]
      });
    }
  };
}

exports.inject = function (module) {
  module.directive('columnPlot', ColumnPlot);
};
