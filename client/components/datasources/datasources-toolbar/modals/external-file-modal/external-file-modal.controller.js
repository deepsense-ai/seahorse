'use strict';

import BaseDatasourceModalController from '../base-datasource-modal-controller.js';

const URL_REGEX = /(http|https|ftp):\/\/[\w\-_]+(\.[\w\-_]+)+([\w\-\\.,@?^=%&amp;:/~\\+#]*[\w\-\\@?^=%&amp;/~\\+#])?/;

class ExternalFileModalController extends BaseDatasourceModalController {
  constructor($scope, $log, $uibModalInstance, datasourcesService, editedDatasource) {
    'ngInject';

    super($log, $uibModalInstance, datasourcesService, editedDatasource);
    this.extension = 'csv';

    if (editedDatasource) {
      this.originalDatasource = editedDatasource;
      this.datasourceParams = editedDatasource.params;
    } else {
      this.datasourceParams = {
        name: '',
        visibility: 'privateVisibility',
        datasourceType: 'externalFile',
        externalFileParams: {
          url: '',
          fileFormat: 'csv',
          csvFileFormatParams: {
            includeHeader: true,
            convert01ToBoolean: false,
            separatorType: 'comma',
            customSeparator: ''
          }
        }
      };
    }

    $scope.$watch(() => this.datasourceParams, (newSettings) => {
      this.datasourceParams = newSettings;
      this.canAddNewDatasource = this.canAddDatasource();
    }, true);
  }

  canAddDatasource() {
    const {separatorType, customSeparator} = this.datasourceParams.externalFileParams.csvFileFormatParams;
    const isSeparatorValid = this.isSeparatorValid(separatorType, customSeparator);
    const isSourceValid = this.datasourceParams.externalFileParams.url !== '';
    const isNameValid = this.datasourceParams.name !== '';

    return isSeparatorValid && isSourceValid && isNameValid && !super.doesNameExists();
  }

  validateUrl() {
    this.isUrlValid = this.datasourceParams.externalFileParams.url !== '' &&
        this.datasourceParams.externalFileParams.url.match(URL_REGEX);
  }

  onFileSettingsChange(data) {
    this.datasourceParams.externalFileParams = Object.assign({}, this.datasourceParams.externalFileParams, data);
  }
}

export default ExternalFileModalController;
