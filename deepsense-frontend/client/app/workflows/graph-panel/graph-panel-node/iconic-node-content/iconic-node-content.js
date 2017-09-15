'use strict';

/*@ngInject*/
function IconicNodeContent() {
  return {
    restrict: 'E',
    templateUrl: 'app/workflows/graph-panel/graph-panel-node/iconic-node-content/iconic-node-content.html'
  };
}

exports.inject = function(module) {
  module.directive('iconicNodeContent', IconicNodeContent);
};
