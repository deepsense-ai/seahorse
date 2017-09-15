/*@ngInject*/
function AttributeCodeSnippetTypeCtrl($uibModal) {

  var that = this;

  that.editInWindow = function() {
    var modalInstance = $uibModal.open({
      animation: true,
      templateUrl: '../../../src/attribute-types/attribute-code-snippet/attribute-code-snippet-type-modal/attribute-code-snippet-type-modal.html',
      controller: 'AttributeCodeSnippetTypeModalCtrl',
      controllerAs: 'acstmCtrl',
      size: 'lg',
      resolve: {
        codeSnippet: function () {
          return {
            code: that.value,
            language: that.language.name.toLowerCase()
          };
        }
      }
    });

    modalInstance.result.then(function (modifiedCode) {
      acstCtrl.value = modifiedCode;
    }, function () {});
  };

}

angular.module('deepsense.attributes-panel').
  controller('AttributeCodeSnippetTypeCtrl', AttributeCodeSnippetTypeCtrl);
