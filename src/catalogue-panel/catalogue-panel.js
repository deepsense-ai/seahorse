function OperationCatalogue () {
  return {
    scope: {
      catalogCollection: '=',
      additionalDirectives: '='
    },
    templateUrl: "catalogue-panel/catalogue-panel.html",
    replace: "true"
  };
}

namespace.directive("operationCatalogue", OperationCatalogue);
