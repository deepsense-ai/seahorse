'use strict';

/* ngInject */
function DocumentationLink(config, version) {
  return {
    restrict: 'E',
    templateUrl: 'app/workflows/workflows-status-bar/documentation-link/documentation-link.html',
    replace: true,
    scope: {},
    link: function (scope) {
      scope.url = config.docsHost + '/docs/' + version.getDocsVersion() + '/index.html';
    }
  };
}

exports.inject = function (module) {
  module.directive('documentationLink', DocumentationLink);
};
