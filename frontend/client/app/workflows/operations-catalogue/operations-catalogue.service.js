'use strict';

class OperationsCatalogueService {
  constructor(Operations) {
    'ngInject';

    this.allOperations = Operations.getCatalog();

    this.visibleLevel = 1;
  }

  getAllOperations() {
    return this.allOperations;
  }

  setVisibleCatalogueLevel(number) {
    this.visibleLevel = number;
  }

  getVisibleCatalogueLevel() {
    return this.visibleLevel;
  }
}

export default OperationsCatalogueService;
