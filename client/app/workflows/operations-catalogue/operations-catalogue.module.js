import angular from 'angular';
import OperationCatalogue from './operations-catalogue.component';
import CategoriesList from './categories-list/categories-list.component';
import CategoryOperationsList from './category-operations-list/category-operations-list.component';
import SearchOperation from './search-operation/search-operation.component';
import OperationsCatalogueService from './operations-catalogue.service.js';

const Operations = angular
  .module('operations-catalogue', [])
  .component('newOperationCatalogue', OperationCatalogue)
  .component('categoriesList', CategoriesList)
  .component('categoryOperationsList', CategoryOperationsList)
  .component('searchOperation', SearchOperation)
  .service('OperationsCatalogueService', OperationsCatalogueService)
  .name;

export default Operations;
