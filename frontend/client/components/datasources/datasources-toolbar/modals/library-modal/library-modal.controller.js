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


class LibraryModalController extends DatasourceModal {
  constructor(
    $scope,
    $log,
    $uibModalInstance,
    LibraryModalService,
    datasourcesService,
    DatasourcesPanelService,
    editedDatasource,
    mode
  ) {
    'ngInject';

    super($log, $uibModalInstance, datasourcesService, editedDatasource, mode);

    this.LibraryModalService = LibraryModalService;
    this.DatasourcesPanelService = DatasourcesPanelService;

    if (editedDatasource) {
      this.originalDatasource = editedDatasource;
      this.datasourceParams = editedDatasource.params;
    } else {
      this.datasourceParams = {
        name: '',
        visibility: 'privateVisibility',
        datasourceType: 'libraryFile',
        libraryFileParams: {
          libraryPath: '',
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

    if (!editedDatasource) {
      this.openLibrary();
    }
  }


  canAddDatasource() {
    const isCsvSeparatorValid = this.isCsvSeparatorValid(this.datasourceParams.libraryFileParams);
    const isSourceValid = this.datasourceParams.libraryFileParams.libraryPath !== '';

    return super.canAddDatasource() &&
      isCsvSeparatorValid &&
      isSourceValid;
  }


  openLibrary() {
    if (this.DatasourcesPanelService.isOpenedForWrite()) {
      this.LibraryModalService.openLibraryModal('write-to-file')
        .then((fullFilePath) => {
          if (fullFilePath) {
            this.setDatasourceParams(fullFilePath);
          } else if (!fullFilePath && !this.datasourceParams.libraryFileParams.libraryPath) {
            this.$uibModalInstance.close();
          }
        });
    } else {
      this.LibraryModalService.openLibraryModal('read-file')
        .then((file) => {
          if (file) {
            this.setDatasourceParams(file.uri);
          } else if (!file && !this.datasourceParams.libraryFileParams.libraryPath) {
            this.$uibModalInstance.close();
          }
        });
    }
  }


  parsePath(filename) {
    const splitFilenameRe = /^(\/?|)([\s\S]*?)((?:\.{1,2}|[^/]+?|)(\.[^./]*|))(?:[/]*)$/;
    const parsedParts = splitFilenameRe.exec(filename).slice(1);

    parsedParts[1] = parsedParts[1] || '';
    parsedParts[2] = parsedParts[2] || '';
    parsedParts[3] = parsedParts[3] || '';

    return {
      root: parsedParts[0],
      dir: parsedParts[0] + parsedParts[1].slice(0, -1),
      base: parsedParts[2],
      ext: parsedParts[3],
      name: parsedParts[2].slice(0, parsedParts[2].length - parsedParts[3].length)
    };
  }


  parseLibraryUri(uri) {
    const filename = uri.replace('library:/', '');

    return this.parsePath(filename);
  }


  setDatasourceParams(fullFilePath) {
    const {name, ext} = this.parseLibraryUri(fullFilePath);

    let fileFormat = ext.slice(1).toLowerCase();
    if (fileFormat !== 'csv' && fileFormat !== 'json') {
      fileFormat = 'csv';
    }

    const libraryFileParams = {
      libraryPath: fullFilePath,
      fileFormat: fileFormat
    };
    if (fileFormat === 'csv') {
      libraryFileParams.csvFileFormatParams = {
        includeHeader: true,
        convert01ToBoolean: false,
        separatorType: 'comma',
        customSeparator: ''
      };
    }
    this.datasourceParams.name = name;

    this.datasourceParams.libraryFileParams = libraryFileParams;
  }


  onFileSettingsChange(newFileSettings) {
    this.datasourceParams.libraryFileParams = Object.assign(
      {
        libraryPath: this.datasourceParams.libraryFileParams.libraryPath
      },
      newFileSettings
    );
  }
}

export default LibraryModalController;
