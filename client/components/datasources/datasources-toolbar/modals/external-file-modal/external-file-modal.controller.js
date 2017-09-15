'use strict';

import footerTpl from '../modal-footer/modal-footer.html';

class ExternalFileModalController {
  constructor($scope, $log, $uibModalInstance, datasourcesService, datasource) {
    'ngInject';

    _.assign(this, {$log, $uibModalInstance, datasourcesService});

    this.footerTpl = footerTpl;
    this.extension = 'csv';

    if (datasource) {
      this.originalDatasource = datasource;
      this.datasourceParams = datasource.params;
    } else {
      this.datasourceParams = {
        name: '',
        visibility: 'privateVisibility',
        datasourceType: 'externalFile',
        externalFileParams: {
          url: '',
          fileFormat: 'csv',
          csvFileFormatParams: {
            includeHeader: false,
            convert01ToBoolean: false,
            separatorType: '',
            customSeparator: ''
          }
        }
      };
    }

    $scope.$watch(() => this.datasourceParams, (newSettings) => {
      this.datasourceParams = newSettings;
      this.canAddNewDatasource = this.checkCanAddNewDatasource();
    }, true);
  }

  checkCanAddNewDatasource() {
    const isSeparatorValid = this.checkIsSeparatorValid();
    const isSourceValid = this.datasourceParams.externalFileParams.url !== '';
    const isNameValid = this.datasourceParams.name !== '';

    return isSeparatorValid && isSourceValid && isNameValid;
  }

  checkIsSeparatorValid() {
    const {separatorType, customSeparator} = this.datasourceParams.externalFileParams.csvFileFormatParams;

    if (separatorType) {
      if (separatorType === 'custom') {
        return customSeparator !== '';
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  onFileSettingsChange(data) {
    this.datasourceParams.externalFileParams = Object.assign({}, this.datasourceParams.externalFileParams, data);
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

export default ExternalFileModalController;
