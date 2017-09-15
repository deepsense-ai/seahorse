function OperationCatalogue () {
  return {
    scope: {
      catalogCollection: '='
    },
    templateUrl: 'catalogue-panel/catalogue-panel.html',
    replace: 'true',
    controllerAs: 'cpCtrl',
    controller: function() {

      var cpCtrl = this;
      cpCtrl.search = '';

      cpCtrl.clearSearch = function() {
        cpCtrl.search = '';
      }


    }
  };
}

namespace.directive("operationCatalogue", OperationCatalogue);
