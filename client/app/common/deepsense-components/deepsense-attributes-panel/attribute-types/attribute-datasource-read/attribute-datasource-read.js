'use strict';

import tpl from './attribute-datasource-read.html';

/* @ngInject */
function AttributeDatasourceRead() {
  return {
    restrict: 'E',
    templateUrl: tpl,
    replace: true
  };
}

angular.module('deepsense.attributes-panel')
  .directive('attributeDatasourceRead', AttributeDatasourceRead);
