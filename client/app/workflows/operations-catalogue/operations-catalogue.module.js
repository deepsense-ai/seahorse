import angular from 'angular';
import OperationCatalogue from './operations-catalogue.component';
import OperationsList from './operations-list/operations-list.component';
import SearchOperation from './search-operation/search-operation.component';
import OperationsCatalogueService from './operations-catalogue.service.js';

const Operations = angular
  .module('operations-catalogue', [])
  .component('operationCatalogue', OperationCatalogue)
  .component('operationsList', OperationsList)
  .component('searchOperation', SearchOperation)
  .service('OperationsCatalogueService', OperationsCatalogueService)
  .name;

export default Operations;
