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

require('./attributes-panel.service.js');

import tpl from './attributes-panel.html';
import {specialOperations} from 'APP/enums/special-operations.js';

/* @ngInject */
function OperationAttributes($rootScope, AttributesPanelService, config, version, Operations) {
  function setCorrectHeight(container) {
    let heightOfOthers = _.reduce(jQuery(
      '> .ibox-title--main, > .c-attributes-tabs',
      container
    ).map((index, el) => $(el).outerHeight(true)), (prev, next) => {
      return prev + next;
    });
    let body = container.querySelector('.ibox-content');

    angular.element(body).css('height', 'calc(100% - ' + heightOfOthers + 'px)');
  }

  return {
    restrict: 'E',
    scope: {
      node: '=',
      isInnerWorkflow: '=',
      publicParams: '=',
      workflowId: '=',
      disabledMode: '='
    },
    templateUrl: tpl,
    replace: false,
    link: (scope, element) => {
      scope.selected = 'parameters';
      scope.publicParams = scope.publicParams || [];

      scope.$watch('node', function () {
        scope.hasCodeEdit = Object.values(specialOperations.NOTEBOOKS).includes(scope.node.operationId);
        scope.hasDocumentation = Operations.get(scope.node.operationId).hasDocumentation;
        scope.$applyAsync(setCorrectHeight.bind(null, element[0]));
      });

      scope.$watch('disabledMode', function () {
        if (scope.disabledMode) {
          AttributesPanelService.setDisabledMode();
          AttributesPanelService.disableElements(element[0]);
        } else {
          AttributesPanelService.enableElements(element[0]);
          AttributesPanelService.setEnabledMode();
        }
        scope.$applyAsync(setCorrectHeight.bind(null, element[0]));
      });
    },

    controller: function ($scope, $sce, $element, $timeout, $uibModal) {
      let getDataFrameSource = () => {
        let incomingEdge = $scope.node.getIncomingEdge(0);
        if (incomingEdge) {
          let {startPortId, startNodeId} = incomingEdge;
          return {nodeId: startNodeId, port: startPortId};
        }
        return {};
      };

      this.getNotebookUrl = () => {
        const languageMap = {
          [specialOperations.NOTEBOOKS.PYTHON]: 'python',
          [specialOperations.NOTEBOOKS.R]: 'r'
        };

        const notebookParams = {
          dataframeSource: getDataFrameSource(),
          language: languageMap[$scope.node.operationId]
        };

        const encodedParams = btoa(JSON.stringify(notebookParams));
        const onlineUrlPart = $scope.disabledMode ? 'OfflineNotebook' : 'notebooks';

        const url = `${config.notebookHost}/${onlineUrlPart}/${$scope.workflowId}/${$scope.node.id}/${encodedParams}`;

        return $sce.trustAsResourceUrl(url);
      };

      this.getDocsHost = () => config.docsHost;
      this.getDocsVersion = () => version.getDocsVersion();

      this.hasCodeEdit = () => $scope.hasCodeEdit;
      this.isInnerWorkflow = () => $scope.isInnerWorkflow;

      this.getVisibility = (parameterName) => {
        let publicParam =
          _.find($scope.publicParams, (pp) => pp.paramName === parameterName && pp.nodeId === $scope.node.id);
        return publicParam ? 'public' : 'private';
      };

      this.setVisibility = (parameterName, visibility) => {
        switch (visibility) {
          case 'public': {
            let publicParam = {
              nodeId: $scope.node.id,
              paramName: parameterName,
              publicName: parameterName
            };
            $scope.publicParams.push(publicParam);
            break;
          }
          case 'private' :
            $scope.publicParams =
              _.reject($scope.publicParams, (pp) => pp.paramName === parameterName && pp.nodeId === $scope.node.id);
            break;
          // no default
        }
      };

      this.showNotebook = () => {
        $scope.modal = $uibModal.open({
          scope: $scope,
          template: `<iframe style="height: calc(100% - 60px); width:100%" frameborder="0" ng-src="{{::controller.getNotebookUrl()}}"></iframe>
                     <button type="button" class="btn btn-default pull-right" ng-click="modal.close()">Close</button>`,
          windowClass: 'o-modal--notebook',
          backdrop: 'static'
        });
      };

      $scope.$on('AttributesPanel.INTERNAL.CLICKED_EDIT_WORKFLOW', (event, data) => {
        $rootScope.$broadcast('AttributesPanel.OPEN_INNER_WORKFLOW', {
          'workflowId': $scope.workflowId,
          'nodeId': $scope.node.id,
          'parameterName': data.parameterName
        });
      });

      this.showErrorMessage = function showErrorMessage() {
        $scope.modal = $uibModal.open({
          size: 'lg',
          scope: $scope,
          template: `
            <button type="button" class="close" aria-label="Close" ng-click="modal.close()">
              <span aria-hidden="true">&times;</span>
            </button>
            <h2>
              Error title:
            </h2>
            <pre class="o-error-trace">{{::node.state.error.title || 'No title'}}</pre>
            <h2>Error message:</h2>
            <pre class="o-error-trace">{{::node.state.error.message || 'No message'}}</pre>
            <div ng-if="::node.state.error.details.stacktrace">
              <h2>Stack trace:</h2>
              <pre class="o-error-trace o-error-full-trace">{{::node.state.error.details.stacktrace}}</pre>
            </div>
            <button type="button" class="btn btn-default pull-right" ng-click="modal.close()">
              Close
            </button>
            <br style="clear: right;" />`,
          windowClass: 'o-modal--error'
        });
      };

      this.nodeNameBuffer = '';
      this.nodeNameInputVisible = false;

      this.showInput = () => {
        this.nodeNameInputVisible = true;
      };

      this.hideInput = () => {
        this.nodeNameInputVisible = false;
      };

      this.enableEdition = () => {
        if (!$scope.disabledMode) {
          this.showInput();
          this.nodeNameBuffer = $scope.node.uiName;
        }
      };

      this.saveNewValue = (event) => {
        if (event.keyCode === 13) { // Enter key code
          $scope.node.uiName = this.nodeNameBuffer;
          this.hideInput();
        } else if (event.keyCode === 27) { // Escape key code
          this.hideInput();
        }
      };

    },
    controllerAs: 'controller'
  };
}

angular.module('deepsense.attributes-panel').directive('deepsenseOperationAttributes', OperationAttributes);
