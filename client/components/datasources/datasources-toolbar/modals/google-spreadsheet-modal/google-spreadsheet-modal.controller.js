'use strict';

import footerTpl from '../modal-footer/modal-footer.html';

class GoogleSpreadsheetModalController {
  constructor($scope, $log, $uibModalInstance, datasourcesService, datasource) {
    'ngInject';

    _.assign(this, {$log, $uibModalInstance, datasourcesService});
    this.footerTpl = footerTpl;

    if (datasource) {
      this.originalDatasource = datasource;
      this.datasourceParams = datasource.params;
    } else {
      this.datasourceParams = {
        name: '',
        visibility: 'privateVisibility',
        datasourceType: 'googleSpreadsheet',
        googleSpreadsheetParams: {
          googleSpreadsheetId: '',
          googleServiceAccountCredentials: ''
        }
      };
    }

    $scope.$watch(() => this.datasourceParams, (newSettings) => {
      this.datasourceParams = newSettings;
      this.canAddNewDatasource = this.checkCanAddNewDatasource();
    }, true);
  }

  checkCanAddNewDatasource() {
    return this.datasourceParams.googleSpreadsheetParams.googleSpreadsheetId !== '' &&
      this.datasourceParams.googleSpreadsheetParams.googleServiceAccountCredentials !== '' &&
      this.datasourceParams.name !== '';
  }

  cancel() {
    this.$uibModalInstance.dismiss();
  }

  ok() {
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

export default GoogleSpreadsheetModalController;
