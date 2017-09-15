'use strict';

import footerTpl from '../modal-footer/modal-footer.html';

class DatabaseModalController {
  constructor($scope, $log, $uibModalInstance, datasourcesService, datasource) {
    'ngInject';

    _.assign(this, {$log, $uibModalInstance, datasourcesService});

    this.footerTpl = footerTpl;
    this.drivers = ['com.mysql.jdbc.driver', 'com.postgresql.jdbc.driver', 'com.oracle.jdbc.driver'];
    this.copyFromQueryInput = true;
    this.sqlInstruction = '';

    if (datasource) {
      this.originalDatasource = datasource;
      this.datasourceParams = datasource.params;
      this.copyFromQueryInput = false;

      if (datasource.params.jdbcParams.query) {
        this.type = 'query';
        this.sqlInstruction = datasource.params.jdbcParams.query;
      } else if (datasource.params.jdbcParams.table) {
        this.type = 'table';
        this.sqlInstruction = datasource.params.jdbcParams.table;
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

    $scope.$watch(() => this.datasourceParams, (newDatasourceParams) => {
      this.datasourceParams = newDatasourceParams;
      this.canAddNewDatasource = this.checkCanAddNewDatasource();
    }, true);

    $scope.$watch(() => this.sqlInstruction, (sqlInstruction) => {
      if (this.copyFromQueryInput) {
        this.datasourceParams.name = sqlInstruction;
      }
    });
  }

  checkCanAddNewDatasource() {
    return this.datasourceParams.name !== '' &&
      this.datasourceParams.jdbcParams.driver !== '' &&
      this.datasourceParams.jdbcParams.url !== '';
  }

  stopCopyingFromUserField() {
    this.copyFromQueryInput = false;
  }

  cancel() {
    this.$uibModalInstance.dismiss();
  }

  ok() {
    if (this.type === 'table') {
      this.datasourceParams.jdbcParams.query = '';
      this.datasourceParams.jdbcParams.table = this.sqlInstruction;
    } else {
      this.datasourceParams.jdbcParams.query = this.sqlInstruction;
      this.datasourceParams.jdbcParams.table = '';
    }

    if (this.originalDatasource) {
      const params = this.datasourceParams;
      const updatedDatasource = Object.assign({}, this.originalDatasource, params);

      this.datasourcesService.updateDatasource(updatedDatasource)
        .then((result) => {
          this.$log.info('result ', result);
          this.$uibModalInstance.close();
        })
        .catch((error) => {
          this.$log.info('error ', error);
        });
    } else {
      this.datasourcesService.addDatasource(this.datasourceParams)
        .then((result) => {
          this.$log.info('result ', result);
          this.$uibModalInstance.close();
        })
        .catch((error) => {
          this.$log.info('error ', error);
        });
    }
  }
}

export default DatabaseModalController;
