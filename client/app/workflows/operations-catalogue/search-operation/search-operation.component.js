require('./search-operation.less');

import tpl from './search-operation.template.html';

const SearchOperationComponent = {
  bindings: {
    searchResults: '<',
    message: '<',
    selectOperation: '&'
  },
  templateUrl: tpl
};

export default SearchOperationComponent;
