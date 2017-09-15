'use strict';

/* @ngInject */
function PortStatusesTooltipController($rootScope, $scope, $element) {
  let that = this;
  let internal = {};

  _.assign(that, {
    portElement: null,
    portObject: null,
    tooltipIsVisible: false,
    flowchartBoxContainer: $($element)
      .closest('.flowchart-box')
  });

  internal.mouseoverHandler = (portEl, portObject) => {
    that.portElement = portEl;
    that.portObject = portObject;
    that.tooltipIsVisible = true;
    $scope.$digest();
  };

  internal.hideTooltip = () => {
    that.portElement = null;
    that.portObject = null;
    that.tooltipIsVisible = false;
    $scope.$digest();
  };

  that.getTypes = () => {
    let outputTypes = _.map(that.portObject.typeQualifier, (typeQualifier) => _.last(typeQualifier.split('.')));
    if (outputTypes.length > 3) {
      outputTypes = outputTypes.slice(0, 3);
      outputTypes.push('...');
    }
    return outputTypes;
  };

  that.getPositionX = () => {
    return Math.round(
      that.portElement.getBoundingClientRect()
      .left -
      that.flowchartBoxContainer[0].getBoundingClientRect()
      .left +
      that.portElement.getBoundingClientRect()
      .width +
      (that.portObject.type === 'output' ? 3 : 0) // a slight adjustment for output ports
    );
  };

  that.getPositionY = () => {
    return Math.round(
      that.portElement.getBoundingClientRect()
      .top -
      that.flowchartBoxContainer[0].getBoundingClientRect()
      .top +
      (that.portObject.type === 'output' ? 10 : 0) // a slight adjustment for output ports
    );
  };

  $rootScope.$on('InputPoint.MOUSEOVER', (event, data) => {
    internal.mouseoverHandler(data.portElement, data.portObject);
  });

  $rootScope.$on('InputPoint.MOUSEOUT', internal.hideTooltip);

  $rootScope.$on('OutputPoint.MOUSEOVER', (event, data) => {
    internal.mouseoverHandler(data.portElement, data.portObject);
  });

  $rootScope.$on('OutputPoint.MOUSEOUT', internal.hideTooltip);

  $scope.$on('Keyboard.KEY_PRESSED_DEL', internal.hideTooltip);

  return that;
}

exports.inject = function(module) {
  module.controller('PortStatusesTooltipController', PortStatusesTooltipController);
};
