'use strict';

class Report {
  constructor() {
    this.restrict = 'E';
    this.replace = true;
    this.scope = {
      currentReport: '=report'
    };
    this.templateUrl = 'app/workflows/reports/reports.html';
    this.controller = 'ReportCtrl as controller';
    this.bindToController = true;
  }

  static directiveFactory() {
    Report.instance = new Report();
    return Report.instance;
  }
}

exports.inject = function(module) {
  module.directive('report', Report.directiveFactory);
};
