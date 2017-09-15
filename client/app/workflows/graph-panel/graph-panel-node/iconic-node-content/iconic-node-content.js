'use strict';

import tpl from './iconic-node-content.html';

/*@ngInject*/
function IconicNodeContent() {
  return {
    restrict: 'E',
    templateUrl: tpl
  };
}

exports.inject = function(module) {
  module.directive('iconicNodeContent', IconicNodeContent);
};
