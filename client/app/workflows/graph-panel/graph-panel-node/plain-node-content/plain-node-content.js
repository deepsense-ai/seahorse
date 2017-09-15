'use strict';

import tpl from './plain-node-content.html';

/*@ngInject*/
function PlainNodeContent() {
  return {
    restrict: 'E',
    templateUrl: tpl
  };
}

exports.inject = function(module) {
  module.directive('plainNodeContent', PlainNodeContent);
};
