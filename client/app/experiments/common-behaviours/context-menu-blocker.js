'use strict';
function ContextMenuBlocker() {
  return {
    restrict: 'A',
    link: function (scope, element, atrributes) {
      console.log(element);
      element.on('contextmenu', (event) => {
        event.preventDefault();
      });
    }
  };
}

exports.inject = function (module) {
  module.directive('contextMenuBlocker', ContextMenuBlocker);
};
