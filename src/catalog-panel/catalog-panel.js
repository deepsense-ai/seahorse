function OperationCatalogue () {
  return {
    scope: {
      catalogCollection: '=',
      additionalDirectives: '='
    },
    templateUrl: "catalog-panel/catalog-panel.html",
    replace: "true"
  };
}

namespace.directive("operationCatalogue", OperationCatalogue);
