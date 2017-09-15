'use strict';

import footerTpl from '../modal-footer/modal-footer.html';

class LibraryModalController {
  constructor($scope, $log, $uibModalInstance, LibraryModalService,
              datasourcesService, datasource, DatasourcesPanelService) {
    'ngInject';

    this.$log = $log;
    this.$uibModalInstance = $uibModalInstance;
    this.LibraryModalService = LibraryModalService;
    this.datasourcesService = datasourcesService;
    this.DatasourcesPanelService = DatasourcesPanelService;

    this.footerTpl = footerTpl;

    if (datasource) {
      this.originalDatasource = datasource;
      this.datasourceParams = datasource.params;
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

    this.openLibrary();

    $scope.$watch(() => this.datasourceParams, (newSettings) => {
      this.datasourceParams = newSettings;
      this.canAddNewDatasource = this.checkCanAddNewDatasource();
    }, true);
  }

  checkCanAddNewDatasource() {
    const isSeparatorValid = this.checkIsSeparatorValid();
    const isSourceValid = this.datasourceParams.libraryFileParams.libraryPath !== '';
    const isNameValid = this.datasourceParams.name !== '';

    return isSeparatorValid && isSourceValid && isNameValid;
  }

  checkIsSeparatorValid() {
    const {separatorType, customSeparator} = this.datasourceParams.libraryFileParams.csvFileFormatParams;

    if (this.extension === 'csv') {
      if (separatorType) {
        return separatorType === 'custom' ? customSeparator !== '' : true;
      } else {
        return false;
      }
    }
    return true;
  }

  openLibrary() {
    if (this.DatasourcesPanelService.isOpenedForWrite()) {
      this.LibraryModalService.openLibraryModal('write-to-file')
        .then((fullFilePath) => {
          if (fullFilePath) {
            this.setParametersForDatasourceParams(fullFilePath);
          }
        });
    } else {
      this.LibraryModalService.openLibraryModal('read-file')
        .then((file) => {
          if (file) {
            this.setParametersForDatasourceParams(file.uri);
          }
        });
    }
  }

  setParametersForDatasourceParams(fullFilePath) {
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

export default LibraryModalController;
