'use strict';

/* @ngInject */
function ReportOptionsService($state) {
  var that = this;
  var internal = {};

  internal.elements = [];
  internal.port = null;
  internal.node = null;

  that.getReportOptions = function getReportOptions() {
    return internal.elements;
  };

  that.getReportId = function getReportId() {
    let currentNode = that.getCurrentNode();
    return currentNode ? currentNode.getResult(that.getCurrentPortIndex()) : '';
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

  that.isCompleted = function isCompleted() {
    var status = that.getCurrentNode().state.status;
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
        visible: true,
        description: 'Show report',
        action: () => {
          $state.go('workflows.reportEntity', {
            reportEntityId: that.getReportId()
          });
        }
      }
    );
  };

  return that;
}

exports.inject = function (module) {
  module.factory('ReportOptionsService', ReportOptionsService);
};
