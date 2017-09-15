require('./operations-catalogue.less');

import tpl from './operations-catalogue.template.html';
import OperationsCatalogueController from './operations-catalogue.controller.js';

const OperationsCatalogueComponent = {
  controller: OperationsCatalogueController,
  bindings: {
    categories: '<',
    onUpdate: '&',
    query: '<',
    selectOperation: '&'
  },
  templateUrl: tpl
};

export default OperationsCatalogueComponent;
