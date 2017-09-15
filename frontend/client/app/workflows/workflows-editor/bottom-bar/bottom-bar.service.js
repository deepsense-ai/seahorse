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

class BottomBarService {
  constructor($rootScope, $timeout) {
    this.$rootScope = $rootScope;
    this.$timeout = $timeout;
    this.tabsState = {
      reportTab: false
    };
  }

  activatePanel(panelName) {
    this.tabsState[panelName] = true;
    let height;
    let bottomTab = JSON.parse(localStorage.getItem('bottomTab'));
    if (!bottomTab || !bottomTab.height) {
      height = 250 + 'px';
    } else {
      height = bottomTab.height + 'px';
    }

    this.$rootScope.$broadcast('Resizable.CHANGE', {
      selector: '.c-workflow-container__content',
      amount: height
    });

    this.$rootScope.$broadcast('Resizable.FIT', {
      name: 'height',
      amount: height,
      selector: '.c-bottom-tabs'
    });
  }

  deactivatePanel(panelName) {
    this.tabsState[panelName] = false;

    this.$rootScope.$broadcast('Resizable.CHANGE', {
      selector: '.c-workflow-container__content',
      amount: '25px'
    });

    this.$rootScope.$broadcast('Resizable.FIT', {
      name: 'height',
      amount: '25px',
      selector: '.c-bottom-tabs'
    });
  }

}

exports.inject = function(module) {
  module.service('BottomBarService', BottomBarService);
};
