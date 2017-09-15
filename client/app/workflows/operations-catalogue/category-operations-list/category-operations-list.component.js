require('./category-operations-list.less');

import tpl from './category-operations-list.template.html';

const CategoryOperationsListComponent = {
  bindings: {
    operations: '<',
    selectOperation: '&'
  },
  templateUrl: tpl
};

export default CategoryOperationsListComponent;
