'use strict';

/* @ngInject */
function PortStatusesTooltipController($scope, $element) {
  let that = this;
  let internal = {};

  _.assign(that, {
    portElement: null,
    portObject: null,
    tooltipIsVisible: false,
    flowchartBoxContainer: $($element).closest('.flowchart-box')
  });

  internal.mouseoverHandler = (portEl, portObject) => {
    that.portElement = portEl;
    that.portObject = portObject;
    that.tooltipIsVisible = true;
    $scope.$digest();
  };

  internal.mouseoutHandler = () => {
    that.portElement = null;
    that.portObject = null;
    that.tooltipIsVisible = false;
    $scope.$digest();
  };

  $scope.$on('InputPoint.MOUSEOVER', (event, data) => { internal.mouseoverHandler(data.portElement, data.portObject); });
  $scope.$on('InputPoint.MOUSEOUT', internal.mouseoutHandler);

  $scope.$on('OutputPoint.MOUSEOVER', (event, data) => { internal.mouseoverHandler(data.portElement, data.portObject); });
  $scope.$on('OutputPoint.MOUSEOUT', internal.mouseoutHandler);

  that.getTypes = () => {
    return _.map(that.portObject.typeQualifier, (typeQualifier) => _.last(typeQualifier.split('.')));
  };

  that.getPositionX = () => {
    return Math.round(
      that.portElement.getBoundingClientRect().left -
      that.flowchartBoxContainer[0].getBoundingClientRect().left +
      that.portElement.getBoundingClientRect().width +
      (that.portObject.type === 'output' ? 3 : 0) // a slight adjustment for output ports
    );
  };

  that.getPositionY = () => {
    return Math.round(
      that.portElement.getBoundingClientRect().top -
      that.flowchartBoxContainer[0].getBoundingClientRect().top +
      (that.portObject.type === 'output' ? 10 : 0) // a slight adjustment for output ports
    );
  };

  return that;
}

exports.inject = function (module) {
  module.controller('PortStatusesTooltipController', PortStatusesTooltipController);
};

