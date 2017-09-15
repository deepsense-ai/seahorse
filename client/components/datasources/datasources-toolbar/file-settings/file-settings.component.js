'use strict';

import templateUrl from './file-settings.html';
import './file-settings.less';

const FileSettingsComponent = {
  bindings: {
    extension: '<',
    onChange: '&'
  },
  templateUrl,
  controller: class FileSettingsController {
    constructor($scope) {
      'ngInject';

      this.fileSettings = {
        selectedFormat: '',
        includesNames: null,
        convertToBool: null,
        separator: ''
      };

      $scope.$watch(() => this.extension, (newValue, oldValue) => {
        if (newValue !== oldValue && newValue === 'csv') {
          this.fileSettings.includesNames = true;
          this.fileSettings.convertToBool = false;
          this.fileSettings.separator = 'coma';
          this.onChange({fileSettings: this.fileSettings});
        }
      });

      $scope.$watch(() => this.fileSettings.separator, (newValue) => {
        if (newValue === 'custom') {
          document.querySelector('#custom-separator').focus();
        }
      });

      this.formats = ['csv', 'json', 'parqet'];
    }

    onFocus() {
      this.focused = true;
      this.fileSettings.separator = 'custom';
    }

    onBlur() {
      this.focused = false;
    }
  }
};

export default FileSettingsComponent;
