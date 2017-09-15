'use strict';

import BaseDatasourceModalController from '../base-datasource-modal-controller.js';

class LibraryModalController extends BaseDatasourceModalController {
  constructor($scope, $log, $uibModalInstance, LibraryModalService,
              datasourcesService, DatasourcesPanelService, editedDatasource) {
    'ngInject';

    super($log, $uibModalInstance, datasourcesService, editedDatasource);

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

    if (!editedDatasource) {
      this.openLibrary();
    }
  }

  canAddDatasource() {
    const {separatorType, customSeparator} = this.datasourceParams.libraryFileParams.csvFileFormatParams;
    const isSeparatorValid = this.isSeparatorValid(separatorType, customSeparator);
    const isSourceValid = this.datasourceParams.libraryFileParams.libraryPath !== '';
    const isNameEmpty = this.datasourceParams.name === '';

    return !super.doesNameExists() && isSeparatorValid && isSourceValid && !isNameEmpty;
  }

  openLibrary() {
    if (this.DatasourcesPanelService.isOpenedForWrite()) {
      this.LibraryModalService.openLibraryModal('write-to-file')
        .then((fullFilePath) => {
          if (fullFilePath) {
            this.setDatasourceParams(fullFilePath);
          }
        });
    } else {
      this.LibraryModalService.openLibraryModal('read-file')
        .then((file) => {
          if (file) {
            this.setDatasourceParams(file.uri);
          }
        });
    }
  }

  setDatasourceParams(fullFilePath) {
    this.extension = fullFilePath.substr(fullFilePath.lastIndexOf('.') + 1);
    this.datasourceParams.libraryFileParams.libraryPath = fullFilePath;
    this.datasourceParams.libraryFileParams.fileFormat = this.extension;
    this.datasourceParams.name = fullFilePath
      .split('.')
      .slice(0, fullFilePath.split('.').length - 1)
      .join('.')
      .replace('library://', '');
  }

  onFileSettingsChange(data) {
    this.datasourceParams.libraryFileParams = Object.assign({}, this.datasourceParams.libraryFileParams, data);
  }
}

export default LibraryModalController;
