'use strict';

/*@ngInject*/
function SourceSinkNodeContent() {
  return {
    restrict: 'E',
    templateUrl: 'app/workflows/graph-panel/graph-panel-node/source-sink-node-content/source-sink-node-content.html'
  };
}

exports.inject = function(module) {
  module.directive('sourceSinkNodeContent', SourceSinkNodeContent);
};
