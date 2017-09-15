/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 */

"use strict";
function OperationCatalogue() {
  return {
    templateUrl: "app/experiments/experiment-editor/catalog-panel/catalog-panel.html",
    replace: "true"
  };
}

exports.inject = function (module) {
  module.directive("operationCatalogue", OperationCatalogue);
};





