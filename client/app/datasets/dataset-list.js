/**
 * Copyright (c) 2015, CodiLime Inc.
 */
'use strict';

/* @ngInject */
function DatasetList(PageService) {
  this.setsLabel = 'You do not have any datasets!';
  PageService.setTitle('Datasets');
}
exports.function = DatasetList;

exports.inject = function (module) {
  module.controller('DatasetList', DatasetList);
};
