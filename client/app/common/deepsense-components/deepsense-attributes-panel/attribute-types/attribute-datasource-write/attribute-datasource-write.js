'use strict';

import tpl from './attribute-datasource-write.html';

/* @ngInject */
function AttributeDatasourceWrite() {
  return {
    restrict: 'E',
    templateUrl: tpl,
    replace: true
  };
}

angular.module('deepsense.attributes-panel')
  .directive('attributeDatasourceWrite', AttributeDatasourceWrite);
