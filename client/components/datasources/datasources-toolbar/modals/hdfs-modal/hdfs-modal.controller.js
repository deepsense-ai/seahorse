'use strict';

import BaseDatasourceModalController from '../base-datasource-modal-controller.js';

class HdfsModalController extends BaseDatasourceModalController {
  constructor($scope, $log, $uibModalInstance, datasourcesService, editedDatasource) {
    'ngInject';

    super($log, $uibModalInstance, datasourcesService, editedDatasource);

    if (editedDatasource) {
      this.originalDatasource = editedDatasource;
      this.datasourceParams = editedDatasource.params;
    } else {
      this.datasourceParams = {
        name: '',
        visibility: 'privateVisibility',
        datasourceType: 'hdfs',
        hdfsParams: {
          hdfsPath: '',
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
      this.canAddNewDatasource = this.canAddDatasource();
    }, true);
  }

  canAddDatasource() {
    const {separatorType, customSeparator} = this.datasourceParams.hdfsParams.csvFileFormatParams;
    const isSeparatorValid = this.isSeparatorValid(separatorType, customSeparator);
    const isSourceValid = this.datasourceParams.hdfsParams.hdfsPath !== '';
    const isNameEmpty = this.datasourceParams.name === '';

    return !super.doesNameExists() && isSeparatorValid && isSourceValid && !isNameEmpty;
  }

  onFileSettingsChange(data) {
    this.datasourceParams.hdfsParams = Object.assign({}, this.datasourceParams.hdfsParams, data);
  }
}

export default HdfsModalController;
