'use strict';

import tpl from './documentation-link.html';

/* ngInject */
function DocumentationLink(config, version) {
  return {
    restrict: 'E',
    templateUrl: tpl,
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
