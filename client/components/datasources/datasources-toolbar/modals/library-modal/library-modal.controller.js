'use strict';

import footerTpl from '../modal-footer/modal-footer.html';

class LibraryModalController {
  constructor($scope, $log, $uibModalInstance, LibraryModalService, datasourcesService) {
    'ngInject';

    _.assign(this, {$log, $uibModalInstance, LibraryModalService, datasourcesService});

    this.footerTpl = footerTpl;

    this.datasourceParams = {
      name: '',
      visibility: 'privateVisibility',
      datasourceType: 'libraryFile',
      libraryFileParams: {
        libraryPath: '',
        fileFormat: '',
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
    const isSourceValid = this.datasourceParams.libraryFileParams.libraryPath !== '';
    const isNameValid = this.datasourceParams.name !== '';

    return isSeparatorValid && isSourceValid && isNameValid;
  }

  checkIsSeparatorValid() {
    const {separatorType, customSeparator} = this.datasourceParams.libraryFileParams.csvFileFormatParams;

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

  openLibrary() {
    this.LibraryModalService.openLibraryModal('read-file')
      .then((file) => {
        this.extension = file.name.substr(file.name.lastIndexOf('.') + 1);
        this.datasourceParams.libraryFileParams.libraryPath = file.uri;
        this.datasourceParams.libraryFileParams.fileFormat = this.extension;
        this.datasourceParams.name = file.name.split('.').slice(0, file.name.split('.').length - 1).join('.');
      });
  }

  onFileSettingsChange(data) {
    this.datasourceParams.libraryFileParams = Object.assign({}, this.datasourceParams.libraryFileParams, data);
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

export default LibraryModalController;
