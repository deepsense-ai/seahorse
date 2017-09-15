'use strict';

function ColumnPlot() {
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
          legend: {
            enabled: false
          },
          tooltip: {
            formatter: function () {
              return `
                <p>Value range:
                  <b>${this.x} - ${data.buckets[this.point.index === data.buckets.length - 1 ? this.point.index : this.point.index + 1]}</b>
                </p>
                <br />
                <p>Occurrence count: <b>${this.point.y}</b></p>
              `;
            }
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
