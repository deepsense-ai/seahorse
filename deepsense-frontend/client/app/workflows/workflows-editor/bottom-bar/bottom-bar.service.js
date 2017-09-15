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

    this.$rootScope.$broadcast('OpenReportTab.SELECT_NODE');
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

    this.$timeout(() => {
      this.$rootScope.$broadcast('AttributePanel.UNSELECT_NODE');
    }, 0);
  }

}

exports.inject = function(module) {
  module.service('BottomBarService', BottomBarService);
};
