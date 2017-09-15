'use strict';

import tpl from './source-sink-node-content.html';

/*@ngInject*/
function SourceSinkNodeContent() {
  return {
    restrict: 'E',
    templateUrl: tpl
  };
}

exports.inject = function(module) {
  module.directive('sourceSinkNodeContent', SourceSinkNodeContent);
};
