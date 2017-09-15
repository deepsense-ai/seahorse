'use strict';

import BaseDatasourceModalController from '../base-datasource-modal-controller.js';

class DatabaseModalController extends BaseDatasourceModalController {
  constructor($scope, $log, $uibModalInstance, datasourcesService, editedDatasource) {
    'ngInject';

    super($log, $uibModalInstance, datasourcesService, editedDatasource);

    this.drivers = ['com.mysql.jdbc.driver', 'com.postgresql.jdbc.driver', 'com.oracle.jdbc.driver'];
    this.copyFromQueryInput = true;
    this.sqlInstruction = '';

    if (editedDatasource) {
      this.originalDatasource = editedDatasource;
      this.datasourceParams = editedDatasource.params;
      this.copyFromQueryInput = false;

      if (editedDatasource.params.jdbcParams.query) {
        this.type = 'query';
        this.sqlInstruction = editedDatasource.params.jdbcParams.query;
      } else if (editedDatasource.params.jdbcParams.table) {
        this.type = 'table';
        this.sqlInstruction = editedDatasource.params.jdbcParams.table;
      }
    } else {
      this.type = 'table';
      this.datasourceParams = {
        name: '',
        visibility: 'privateVisibility',
        datasourceType: 'jdbc',
        jdbcParams: {
          driver: this.drivers[0],
          url: '',
          query: '',
          table: ''
        }
      };
    }

    $scope.$watch(() => this.sqlInstruction, (sqlInstruction) => {
      if (this.copyFromQueryInput) {
        this.datasourceParams.name = sqlInstruction;
      }
    });

    $scope.$watch(() => this.datasourceParams, (newSettings) => {
      this.datasourceParams = newSettings;
      this.canAddNewDatasource = this.canAddDatasource();
    }, true);
  }

  canAddDatasource() {
    return this.datasourceParams.name !== '' &&
      this.datasourceParams.jdbcParams.driver !== '' &&
      this.datasourceParams.jdbcParams.url !== '' &&
      !super.doesNameExists();
  }

  stopCopyingFromUserField() {
    this.copyFromQueryInput = false;
  }

  onQueryTypeChange() {
    if (this.type === 'table') {
      this.datasourceParams.jdbcParams.query = '';
      this.datasourceParams.jdbcParams.table = this.sqlInstruction;
    } else {
      this.datasourceParams.jdbcParams.query = this.sqlInstruction;
      this.datasourceParams.jdbcParams.table = '';
    }
  }
}

export default DatabaseModalController;
