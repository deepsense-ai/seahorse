'use strict';

function ContextMenuBlocker() {
  return {
    restrict: 'A',
    link: function(scope, element, atrributes) {
      element.on('contextmenu', (event) => {
        event.preventDefault();
      });
    }
  };
}

exports.inject = function(module) {
  module.directive('contextMenuBlocker', ContextMenuBlocker);
};
