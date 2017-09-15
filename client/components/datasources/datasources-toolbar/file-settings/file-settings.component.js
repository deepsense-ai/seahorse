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
        fileFormat: '',
        csvFileFormatParams: {
          includeHeader: false,
          convert01ToBoolean: false,
          separatorType: '',
          customSeparator: ''
        }
      };

      $scope.$watch(() => this.extension, (newValue, oldValue) => {
        if (newValue !== oldValue && newValue === 'csv') {
          this.fileSettings.csvFileFormatParams.includeHeader = true;
          this.fileSettings.csvFileFormatParams.convert01ToBoolean = false;
          this.fileSettings.csvFileFormatParams.separatorType = 'comma';
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
      this.fileSettings.csvFileFormatParams.separator = 'custom';
    }
  }
};

export default FileSettingsComponent;
