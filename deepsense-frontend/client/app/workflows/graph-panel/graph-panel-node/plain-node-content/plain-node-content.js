'use strict';

/*@ngInject*/
function PlainNodeContent() {
  return {
    restrict: 'E',
    templateUrl: 'app/workflows/graph-panel/graph-panel-node/plain-node-content/plain-node-content.html'
  };
}

exports.inject = function(module) {
  module.directive('plainNodeContent', PlainNodeContent);
};
