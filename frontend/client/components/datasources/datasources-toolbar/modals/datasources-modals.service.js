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
import databaseTpl from './database-modal/database-modal.html';
import externalFileTpl from './external-file-modal/external-file-modal.html';
import googleSpreadsheetTpl from './google-spreadsheet-modal/google-spreadsheet-modal.html';
import hdfsTpl from './hdfs-modal/hdfs-modal.html';
import libraryTpl from './library-modal/library-modal.html';

// App
import {datasourceModalMode} from 'COMMON/datasources/datasource-modal-mode.js';


const MODAL_CONFIGS = {
  jdbc: {
    tpl: databaseTpl,
    ctrl: 'DatabaseModalController'
  },
  externalFile: {
    tpl: externalFileTpl,
    ctrl: 'ExternalFileModalController'
  },
  googleSpreadsheet: {
    tpl: googleSpreadsheetTpl,
    ctrl: 'GoogleSpreadsheetModalController'
  },
  hdfs: {
    tpl: hdfsTpl,
    ctrl: 'HdfsModalController'
  },
  libraryFile: {
    tpl: libraryTpl,
    ctrl: 'LibraryModalController'
  }
};


class DatasourcesModalsService {
  constructor($uibModal, $document, $log) {
    'ngInject';

    this.$uibModal = $uibModal;
    this.$document = $document;
    this.$log = $log;
  }


  openModal(datasourceType, mode, datasource) {
    this.$log.info('DatasourcesModalsService.openModal()', mode, datasourceType);

    const modal = MODAL_CONFIGS[datasourceType];
    const $datasourcesToolbar = angular.element(document.querySelector('.datasources-panel'));

    return this.$uibModal.open({
      appendTo: $datasourcesToolbar,
      windowClass: 'panel-datasource-modal',
      animation: true,
      templateUrl: modal.tpl,
      size: 'lg',
      controller: modal.ctrl,
      controllerAs: '$ctrl',
      backdrop: 'static',
      keyboard: true,
      resolve: {
        editedDatasource: () => angular.copy(datasource),
        mode: () => mode
      }
    });
  }
}

export default DatasourcesModalsService;
