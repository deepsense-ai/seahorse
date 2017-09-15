'use strict';


function DynamicParamTypeService() {
  /**
   * These watches perform equality-wise two-way binding.
   * When parameter.internalParams is changed, bufferedInternalParams is changed only
   * when new value is different than old value in terms of ===.
   * The same works in the opposite direction.
   *
   * This trick allows to avoid reloading dynamic and gridsearch
   * parameters while editing them and nothing changes in the schema.
   */
  this.addBufferedInternalParamsWatches = function (directiveScope, containerScope) {
    containerScope.$watch('parameter.internalParams', (newValue, oldValue) => {
      let cleanBufferedInternalParams = angular.toJson(directiveScope.bufferedInternalParams);
      let cleanNewValue = angular.toJson(newValue);
      if (cleanBufferedInternalParams !== cleanNewValue) {
        directiveScope.bufferedInternalParams = newValue;
      }
    }, true);
    directiveScope.$watch('bufferedInternalParams', (newValue, oldValue) => {
      let cleanNewValue = angular.toJson(newValue);
      let cleanInternalParamsValue = angular.toJson(containerScope.parameter.internalParams);
      if (cleanNewValue !== cleanInternalParamsValue) {
        containerScope.parameter.internalParams = newValue;
      }
    }, true);
  };
}

angular.module('deepsense.attributes-panel')
  .service('DynamicParamTypeService', DynamicParamTypeService);
