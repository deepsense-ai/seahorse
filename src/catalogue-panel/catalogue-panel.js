function OperationCatalogue () {
  return {
    scope: {
      catalogCollection: '='
    },
    templateUrl: "catalogue-panel/catalogue-panel.html",
    replace: "true"
  };
}

namespace.directive("operationCatalogue", OperationCatalogue);
