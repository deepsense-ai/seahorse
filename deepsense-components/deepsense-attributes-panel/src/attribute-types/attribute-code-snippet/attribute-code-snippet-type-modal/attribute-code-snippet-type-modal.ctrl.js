/*@ngInject*/
function AttributeCodeSnippetTypeModalCtrl($uibModalInstance, codeSnippet) {
  var that = this;

  that.codeSnippet = codeSnippet;

  that.cancel = function () {
    $uibModalInstance.dismiss();
  };

  that.ok = function () {
    $uibModalInstance.close(that.codeSnippet.code);
  }

}

angular.module('deepsense.attributes-panel').
controller('AttributeCodeSnippetTypeModalCtrl', AttributeCodeSnippetTypeModalCtrl);

