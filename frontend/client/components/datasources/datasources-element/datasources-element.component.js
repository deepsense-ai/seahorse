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
import templateUrl from './datasources-element.html';
import './datasources-element.less';

// App
import {datasourceModalMode} from 'COMMON/datasources/datasource-modal-mode.js';
import {datasourceContext} from 'APP/enums/datasources-context.js';


const COOKIE_NAME = 'DELETE_DATASOURCE_COOKIE';
const DATASOURCE_ICON = {
  externalFile: 'sa-external-file',
  libraryFile: 'sa-library',
  hdfs: 'sa-hdfs',
  jdbc: 'sa-database',
  googleSpreadsheet: 'sa-google-spreadsheet'
};


const DatasourcesElementComponent = {
  templateUrl,

  bindings: {
    context: '<',
    datasource: '<',
    onSelect: '&'
  },

  controller: class DatasourcesElementController {
    constructor(
      DeleteModalService,
      DatasourcesModalsService,
      datasourcesService
    ) {
      'ngInject';

      this.DeleteModalService = DeleteModalService;
      this.DatasourcesModalsService = DatasourcesModalsService;
      this.datasourcesService = datasourcesService;

      this.visibilityLabel = {
        publicVisibility: 'Public',
        privateVisibility: 'Private'
      };
    }


    $onInit() {
      this.datasourceIcon = DATASOURCE_ICON[this.datasource.params.datasourceType];
      this.context = this.context || datasourceContext.BROWSE_DATASOURCE;

      if (this.isOwner()) {
        this.ownerName = 'You';
        this.openActionIcon = 'sa-edit';
        this.openActionTitle = 'Edit';
      } else {
        this.ownerName = this.datasource.ownerName;
        this.openActionIcon = 'sa-view';
        this.openActionTitle = 'See';
      }
    }


    deleteDatasource() {
      this.DeleteModalService.handleDelete(() => {
        this.datasourcesService.deleteDatasource(this.datasource.id);
      }, COOKIE_NAME);
    }


    isOwner() {
      return this.datasourcesService.isCurrentUserOwnerOfDatasource(this.datasource);
    }


    isSelectable() {
      if (this.context === datasourceContext.WRITE_DATASOURCE) {
        return this.datasource.params.datasourceType !== 'externalFile';
      }
      return this.context !== datasourceContext.BROWSE_DATASOURCE;
    }


    openDatasource() {
      const datasourceType = this.datasource.params.datasourceType;
      const mode = this.isOwner() ? datasourceModalMode.EDIT : datasourceModalMode.VIEW;

      this.DatasourcesModalsService.openModal(datasourceType, mode, this.datasource);
    }


    selectDatasource() {
      if (this.isSelectable()) {
        this.onSelect({ datasource: this.datasource });
      }
    }
  }
};

export default DatasourcesElementComponent;
