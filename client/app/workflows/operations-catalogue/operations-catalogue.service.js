'use strict';

class OperationsCatalogueService {
  constructor(Operations) {
    'ngInject';

    this.allOperations = Operations.getCatalog();
  }

  getAllOperations() {
    return this.allOperations;
  }
}

export default OperationsCatalogueService;
