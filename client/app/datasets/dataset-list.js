/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function DatasetList() {
  this.setsLabel = 'You do not have any datasets!';
}
exports.function = DatasetList;

exports.inject = function (module) {
  module.controller('DatasetList', DatasetList);
};
