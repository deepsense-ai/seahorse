'use strict';

import templateUrl from './file-settings.html';
import './file-settings.less';

const FileSettingsComponent = {
  bindings: {},
  templateUrl,
  controller: class FileSettingsController {
    constructor() {
      'ngInject';

      this.formats = ['CSV', 'JSON', 'PARQET'];
    }

  }
};

export default FileSettingsComponent;
