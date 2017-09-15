/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function Report($state, EntitiesAPIClient) {

  var that = this;
  var internal = {};

  internal.closeReport = function closeReport () {
    console.log($state);
    // TODO $state go to previous page ... now it is some error with approach $state.go('^')
  };

  EntitiesAPIClient.getReport('test-01').then((data) => {
    internal.tables = data.tables;
  }, (error) => {
    console.log('error', error);
  });

  that.getData = function getData() {
    return internal.tables && internal.tables.DataSample;
  };

  that.dismiss = function dismiss () {
    internal.closeReport();
  };

  return that;
}

exports.function = Report;

exports.inject = function (module) {
  module.controller('Report', Report);
};
