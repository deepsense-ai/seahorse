require('./categories-list.less');

import tpl from './categories-list.template.html';
import CategoriesListController from './categories-list.controller.js';

const CategoriesListComponent = {
  controller: CategoriesListController,
  bindings: {
    categories : '<',
    selectOperation: '&'
  },
  templateUrl: tpl
};

export default CategoriesListComponent;
