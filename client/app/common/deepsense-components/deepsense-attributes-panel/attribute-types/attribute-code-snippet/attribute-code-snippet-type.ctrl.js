'use strict';

require('./attribute-code-snippet-type-modal/attribute-code-snippet-type-modal.ctrl.js');

import tpl from './attribute-code-snippet-type-modal/attribute-code-snippet-type-modal.html';

/*@ngInject*/
function AttributeCodeSnippetTypeCtrl($uibModal) {

  var that = this;

  that.editInWindow = function() {
    var modalInstance = $uibModal.open({
      animation: true,
      backdrop: 'static',
      templateUrl: tpl,
      controller: 'AttributeCodeSnippetTypeModalCtrl',
      controllerAs: 'acstmCtrl',
      size: 'lg',
      resolve: {
        codeSnippet: function () {
          return {
            code: that.value,
            language: that.language.toLowerCase()
          };
        }
      }
    });

    modalInstance.result.then(function (modifiedCode) {
      if(that.value !== modifiedCode) {
        that.value = modifiedCode;
      }
    }, function () {});
  };

}

angular.module('deepsense.attributes-panel').
  controller('AttributeCodeSnippetTypeCtrl', AttributeCodeSnippetTypeCtrl);


