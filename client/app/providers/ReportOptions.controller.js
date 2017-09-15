'use strict';

/* @ngInject */
function ReportOptionsController($scope, $rootScope, ReportOptionsService, ExperimentService) {
  var that = this;

  $scope.$on('OutputPort.RIGHT_CLICK', function (event, data) {
    let port = data.reference;
    let nodeId = port.getParameter('nodeId');
    let currentNode = ExperimentService.getExperiment().getNodes()[nodeId];

    ReportOptionsService.setCurrentPort(port);
    ReportOptionsService.setCurrentNode(currentNode);
    ReportOptionsService.clearReportOptions();
    ReportOptionsService.updateReportOptions();

    $rootScope.$broadcast('ReportOptions.UPDATED', data);
  });

  that.getReportOptions = function getReportOptions() {
    return ReportOptionsService.getReportOptions();
  };

  return that;
}

exports.inject = function (module) {
  module.controller('ReportOptionsController', ReportOptionsController);
};

