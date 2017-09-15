'use strict';
function ReportOptionsService($rootScope) {
  var that = this;
  var internal = {};

  internal.elements = [];
  internal.port = null;
  internal.node = null;
  internal.IS_DEPLOYABLE = 'io.deepsense.deeplang.doperables.Scorable';

  internal.goToDeploy = function goToDeploy() {
    $rootScope.$broadcast('Model.DEPLOY', {id: that.getReportId()});
  };

  that.getReportOptions = function getReportOptions() {
    return internal.elements;
  };

  that.getReportURL = function getReportURL() {
    return '/#/report/' + that.getReportId();
  };

  that.getReportId = function getReportId() {
    if (that.getCurrentNode().results && that.getCurrentNode().results.length > 0) {
      return that.getCurrentNode().results[that.getCurrentPortIndex()];
    }
    else {
      return '';
    }
  };

  that.getCurrentNode = function getCurrentNode() {
    return internal.node;
  };

  that.getCurrentPort = function getCurrentPort() {
    return internal.port;
  };

  that.getCurrentPortIndex = function getCurrentPort() {
    return that.getCurrentPort().getParameter('portIndex');
  };

  that.isDeployable = function isDeployable() {
    let nodeOutputPorts = that.getCurrentNode().output;
    let currentPort = nodeOutputPorts[that.getCurrentPortIndex()];
    return currentPort.typeQualifier.indexOf(internal.IS_DEPLOYABLE) > -1;
  };

  that.isCompleted = function isCompleted() {
    var status = that.getCurrentNode().status;
    return status === that.getCurrentNode().STATUS.COMPLETED;
  };

  that.setCurrentPort = function setCurrentPort(port) {
    internal.port = port;
  };

  that.setCurrentNode = function setCurrentNode(node) {
    internal.node = node;
  };

  that.clearReportOptions = function clearReportOptions() {
    that.getReportOptions().length = 0;
  };

  that.updateReportOptions = function updateReportOptions() {
    internal.elements.push({
        icon: 'fa-dot-circle-o',
        active: that.isCompleted(),
        href: that.getReportURL(),
        visible: true,
        description: 'Show report'
      }, {
        icon: 'fa-database',
        active: that.isDeployable(),
        description: 'Deploy',
        visible: that.isDeployable(),
        action: internal.goToDeploy
      }
    );
  };

  return that;
}

exports.inject = function (module) {
  module.factory('ReportOptionsService', ReportOptionsService);
};
