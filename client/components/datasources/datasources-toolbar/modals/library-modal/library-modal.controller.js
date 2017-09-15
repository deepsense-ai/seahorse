'use strict';

import footerTpl from '../modal-footer/modal-footer.html';

class LibraryModalController {
  constructor(LibraryModalService, $scope, $uibModalInstance) {
    'ngInject';

    this.footerTpl = footerTpl;

    this.settings = {
      source: '',
      fileSettings: {
        selectedFormat: '',
        includesNames: false,
        convertToBool: false,
        separator: ''
      },
      publicData: false
    };

    $scope.$watch(() => this.settings, (newSettings) => {
      this.settings = newSettings;
      this.canAddNewDatasource = this.checkCanAddNewDatasource();
    }, true);

    _.assign(this, {LibraryModalService, $uibModalInstance});
  }

  checkCanAddNewDatasource() {
    const isSeparatorValid = this.checkIsSeparatorValid();
    const isSourceValid = this.settings.source !== '';

    return isSeparatorValid && isSourceValid;
  }

  checkIsSeparatorValid() {
    const {separator, customSeparator} = this.settings.fileSettings;

    if (separator) {
      if (separator === 'custom') {
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
        this.settings.source = file.uri;
        this.extension = file.name.substr(file.name.lastIndexOf('.') + 1);
      });
  }

  onFileSettingsChange(data) {
    this.settings.fileSettings = data;
  }

  cancel() {
    this.$uibModalInstance.dismiss();
  }

  ok() {
    this.$uibModalInstance.close();
  }
}

export default LibraryModalController;
