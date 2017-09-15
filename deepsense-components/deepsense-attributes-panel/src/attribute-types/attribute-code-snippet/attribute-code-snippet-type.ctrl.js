/*@ngInject*/
function AttributeCodeSnippetTypeCtrl($rootScope, $uibModal) {

  var that = this;

  that.editInWindow = function() {
    var modalInstance = $uibModal.open({
      animation: true,
      backdrop: 'static',
      templateUrl: 'attribute-types/attribute-code-snippet/attribute-code-snippet-type-modal/attribute-code-snippet-type-modal.html',
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
