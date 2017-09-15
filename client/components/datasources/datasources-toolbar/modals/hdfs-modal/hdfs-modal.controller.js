'use strict';

import footerTpl from '../modal-footer/modal-footer.html';

class HdfsModalController {
  constructor($scope, $log, $uibModalInstance, datasourcesService) {
    'ngInject';

    _.assign(this, {$log, $uibModalInstance, datasourcesService});

    this.footerTpl = footerTpl;
    this.extension = 'csv';

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

    $scope.$watch(() => this.datasourceParams, (newSettings) => {
      this.datasourceParams = newSettings;
      this.canAddNewDatasource = this.checkCanAddNewDatasource();
    }, true);
  }

  checkCanAddNewDatasource() {
    const isSeparatorValid = this.checkIsSeparatorValid();
    const isSourceValid = this.datasourceParams.hdfsParams.hdfsPath !== '';
    const isNameValid = this.datasourceParams.name !== '';

    return isSeparatorValid && isSourceValid && isNameValid;
  }

  checkIsSeparatorValid() {
    const {separatorType, customSeparator} = this.datasourceParams.hdfsParams.csvFileFormatParams;

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
    this.datasourceParams.hdfsParams = Object.assign({}, this.datasourceParams.hdfsParams, data);
  }

  cancel() {
    this.$uibModalInstance.dismiss();
  }

  ok() {
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

export default HdfsModalController;
