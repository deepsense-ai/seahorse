/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

function DistributionCategoricalChart() {
  return {
    restrict: 'E',
    templateUrl: 'app/reports/charts/distribution-categorical-chart.html',
    replace: true,
    scope: false,
    link: function (scope, element, attrs) {
      let distObject = scope.distObject;

      $(element).highcharts({
        chart: {
          type: 'pie'
        },
        title: {
          text: distObject.name
        },
        subtitle: {
          text: distObject.description
        },
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
          pointFormat: 'The number of people who listen to this kind of music: <b>{point.y}</b>'
        },
        series: [{
          name: 'Population',
          colorByPoint: true,
          data: _.zip(distObject.buckets, distObject.counts)
        }]
      });
    }
  };
}

exports.inject = function (module) {
  module.directive('distributionCategoricalChart', DistributionCategoricalChart);
};
