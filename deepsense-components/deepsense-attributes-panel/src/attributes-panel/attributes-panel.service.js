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
      jQuery(':input:not(.o-error-btn), textarea', container)
        .attr('disabled', 'disabled');
    }
  }

  enableElements (container) {
    jQuery(':input:not(.o-error-btn), textarea', container)
      .removeAttr('disabled');
  }
}

angular.module('deepsense.attributes-panel')
  .service('AttributesPanelService', AttributesPanelService);
