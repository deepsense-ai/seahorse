'use strict';

import DatasourceModal from '../datasource-modal.class.js';

const HDFS_REGEX = /(hdfs):\/\/([\w\-_]+)+([\w\-\\.,@?^=%&amp;:/~\\+#]*[\w\-\\@?^=%&amp;/~\\+#])?/;
const HDFS_PREFIX = 'hdfs://';

class HdfsModalController extends DatasourceModal {
  constructor($scope, $log, $uibModalInstance, datasourcesService, editedDatasource, previewMode) {
    'ngInject';

    super($log, $uibModalInstance, datasourcesService, editedDatasource, previewMode);

    if (editedDatasource) {
      this.originalDatasource = editedDatasource;
      this.datasourceParams = editedDatasource.params;
      this.hdfsPathBuffer = this.datasourceParams.hdfsParams.hdfsPath.toLowerCase().replace(HDFS_PREFIX, '');
      this.validateHdfsPath();
    } else {
      this.datasourceParams = {
        name: '',
        visibility: 'privateVisibility',
        datasourceType: 'hdfs',
        hdfsParams: {
          hdfsPath: '',
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
    const {separatorType, customSeparator} = this.datasourceParams.hdfsParams.csvFileFormatParams;
    const isSeparatorValid = this.isSeparatorValid(separatorType, customSeparator);
    const isSourceValid = this.datasourceParams.hdfsParams.hdfsPath !== '';
    const isNameEmpty = this.datasourceParams.name === '';

    return !super.doesNameExists() && isSeparatorValid && isSourceValid && !isNameEmpty;
  }

  onChangeHandler() {
    this.hideHdfsPrefix();
    this.validateHdfsPath();
  }

  hideHdfsPrefix() {
    this.hdfsPathBuffer = this.hdfsPathBuffer.toLowerCase().replace(HDFS_PREFIX, '');
    this.datasourceParams.hdfsParams.hdfsPath = `${HDFS_PREFIX}${this.hdfsPathBuffer}`;
  }

  validateHdfsPath() {
    this.isHdfsPathValid = this.datasourceParams.hdfsParams.hdfsPath !== '' &&
      this.datasourceParams.hdfsParams.hdfsPath.match(HDFS_REGEX);
  }

  onFileSettingsChange(data) {
    this.datasourceParams.hdfsParams = Object.assign({}, this.datasourceParams.hdfsParams, data);
  }
}

export default HdfsModalController;
