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

// Assets
import './datasources-toolbar.less';
import './modals/datasources-modals.less';
import './modals/modal-footer/modal-footer.less';
import './modals/external-file-modal/external-file-modal.less';
import './modals/hdfs-modal/hdfs-modal.less';
import './modals/library-modal/library-modal.less';
import templateUrl from './datasources-toolbar.html';

// App
import {datasourceModalMode} from 'COMMON/datasources/datasource-modal-mode.js';
import {datasourceContext} from 'APP/enums/datasources-context.js';


const DatasourcesToolbarComponent = {
  templateUrl,

  bindings: {
    context: '<'
  },

  controller: class DatasourcesToolbarController {
    constructor(
      $scope,
      DatasourcesModalsService,
      LibraryService
    ) {
      'ngInject';

      this.DatasourcesModalsService = DatasourcesModalsService;
      this.datasourceContext = datasourceContext;

      $scope.$watch(LibraryService.isUploadingInProgress, (newValue) => {
        this.uploadingInProgress = newValue;
      }, true);
    }


    addDatasource(datasourceType) {
      this.DatasourcesModalsService.openModal(datasourceType, datasourceModalMode.ADD);
    }
  }

};

export default DatasourcesToolbarComponent;
