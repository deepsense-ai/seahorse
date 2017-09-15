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


const URL_REGEX = /(http|https|ftp):\/\/[\w\-_]+(\.[\w\-_]+)+([\w\-\\.,@?^=%&amp;:/~\\+#]*[\w\-\\@?^=%&amp;/~\\+#])?/;


class ExternalFileModalController extends DatasourceModal {
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
      this.validateUrl();
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
    const isCsvSeparatorValid = this.isCsvSeparatorValid(this.datasourceParams.externalFileParams);
    const isSourceValid = this.datasourceParams.externalFileParams.url !== '';

    return super.canAddDatasource() &&
      isCsvSeparatorValid &&
      isSourceValid;
  }


  validateUrl() {
    this.isUrlValid = this.datasourceParams.externalFileParams.url !== '' &&
        !!this.datasourceParams.externalFileParams.url.match(URL_REGEX);
  }


  onFileSettingsChange(newFileSettings) {
    this.datasourceParams.externalFileParams = Object.assign(
      {
        url: this.datasourceParams.externalFileParams.url
      },
      newFileSettings
    );
  }
}

export default ExternalFileModalController;
