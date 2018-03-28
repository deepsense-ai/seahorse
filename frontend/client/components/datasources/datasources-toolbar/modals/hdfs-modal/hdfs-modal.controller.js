/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

// App
import DatasourceModal from '../datasource-modal.class.js';


const HDFS_REGEX = /(hdfs):\/\/([\w\-_]+)+([\w\-\\.,@?^=%&amp;:/~\\+#]*[\w\-\\@?^=%&amp;/~\\+#])?/;
const HDFS_PREFIX = 'hdfs://';


class HdfsModalController extends DatasourceModal {
  constructor(
    $scope,
    $log,
    $uibModalInstance,
    datasourcesService,
    editedDatasource,
    mode
  ) {
    'ngInject';

    super($log, $uibModalInstance, datasourcesService, editedDatasource, mode);

    if (editedDatasource) {
      this.originalDatasource = editedDatasource;
      this.datasourceParams = editedDatasource.params;
      this.hdfsPathBuffer = this.datasourceParams.hdfsParams.hdfsPath.replace(HDFS_PREFIX, '');
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
    const isCsvSeparatorValid = this.isCsvSeparatorValid(this.datasourceParams.hdfsParams);
    const isSourceValid = this.datasourceParams.hdfsParams.hdfsPath !== 'hdfs://';

    return super.canAddDatasource() &&
      isCsvSeparatorValid &&
      isSourceValid;
  }


  onChangeHandler() {
    this.hideHdfsPrefix();
    this.validateHdfsPath();
  }


  hideHdfsPrefix() {
    this.hdfsPathBuffer = this.hdfsPathBuffer.replace(HDFS_PREFIX, '');
    this.datasourceParams.hdfsParams.hdfsPath = `${HDFS_PREFIX}${this.hdfsPathBuffer}`;
  }


  validateHdfsPath() {
    this.isHdfsPathValid = this.datasourceParams.hdfsParams.hdfsPath !== '' &&
      this.datasourceParams.hdfsParams.hdfsPath.match(HDFS_REGEX);
  }


  onFileSettingsChange(newFileSettings) {
    this.datasourceParams.hdfsParams = Object.assign(
      {
        hdfsPath: this.datasourceParams.hdfsParams.hdfsPath
      },
      newFileSettings
    );
  }
}

export default HdfsModalController;
