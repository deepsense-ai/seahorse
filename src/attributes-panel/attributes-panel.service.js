/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by Oleksandr Tserkovnyi on 03.09.15.
 */

class AttributesPanelService {
  /*@ngInject*/
  constructor () {
    this.disabledMode = false;
  }

  setDisabledMode () {
    this.disabledMode = true;
  }

  setEnabledMode () {
    this.disabledMode = false;
  }

  getDisabledMode () {
    return this.disabledMode;
  }

  disableElements (container) {
    if (this.getDisabledMode()) {
      jQuery(':input, textarea', container)
        .attr('disabled', 'disabled');
      jQuery('.close-link', container)
        .css('display', 'none');
    }
  }
}

angular.module('deepsense.attributes-panel')
  .service('AttributesPanelService', AttributesPanelService);