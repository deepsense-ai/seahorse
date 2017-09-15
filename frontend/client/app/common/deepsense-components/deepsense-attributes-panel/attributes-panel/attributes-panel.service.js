'use strict';

function AttributesPanelService() {
  this.disabledMode = false;

  this.setDisabledMode = function () {
    this.disabledMode = true;
  };

  this.setEnabledMode = function () {
    this.disabledMode = false;
  };

  this.getDisabledMode = function () {
    return this.disabledMode;
  };

  this.disableElements = function (container) {
    if (this.getDisabledMode()) {
      jQuery(':input:not(.o-error-btn), textarea', container)
        .attr('disabled', 'disabled');
    }
  };

  this.enableElements = function (container) {
    jQuery(':input:not(.o-error-btn), textarea', container)
      .removeAttr('disabled');
  };
}

angular.module('deepsense.attributes-panel')
  .service('AttributesPanelService', AttributesPanelService);
