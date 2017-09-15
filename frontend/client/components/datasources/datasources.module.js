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

// Libs
import angular from 'angular';

// App
import DatasourcesPanelComponent from './datasources-panel/datasources-panel.component.js';
import DatasourcesElementComponent from './datasources-element/datasources-element.component.js';
import DatasourcesListComponent from './datasources-list/datasources-list.component.js';
import DatasourcesToolbarComponent from './datasources-toolbar/datasources-toolbar.component.js';
import FileSettingsComponent from './datasources-toolbar/file-settings/file-settings.component.js';
import DatasourcesPanelService from './datasources-panel/datasources-panel.service.js';
import DatasourcesModalsService from './datasources-toolbar/modals/datasources-modals.service.js';

// Modals controllers
import DatabaseModalController from './datasources-toolbar/modals/database-modal/database-modal.controller.js';
import ExternalFileModalController from './datasources-toolbar/modals/external-file-modal/external-file-modal.controller.js';
import GoogleSpreadsheetModalController from './datasources-toolbar/modals/google-spreadsheet-modal/google-spreadsheet-modal.controller.js';
import HdfsModalController from './datasources-toolbar/modals/hdfs-modal/hdfs-modal.controller.js';
import LibraryModalController from './datasources-toolbar/modals/library-modal/library-modal.controller.js';

export const datasourcesModule = angular
  .module('datasources', [])
  .component('datasourcesPanel', DatasourcesPanelComponent)
  .component('datasourcesElement', DatasourcesElementComponent)
  .component('datasourcesList', DatasourcesListComponent)
  .component('datasourcesToolbar', DatasourcesToolbarComponent)
  .component('fileSettings', FileSettingsComponent)
  .service('DatasourcesPanelService', DatasourcesPanelService)
  .service('DatasourcesModalsService', DatasourcesModalsService)
  .controller('DatabaseModalController', DatabaseModalController)
  .controller('ExternalFileModalController', ExternalFileModalController)
  .controller('GoogleSpreadsheetModalController', GoogleSpreadsheetModalController)
  .controller('HdfsModalController', HdfsModalController)
  .controller('LibraryModalController', LibraryModalController)
  .name;
