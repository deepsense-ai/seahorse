'use strict';

class ResizableListener {
  constructor() {
    this.restrict = 'A';
  }

  changeHeight(element, amount) {
    if (typeof amount !== 'string') {
      amount = amount + 'px';
    }

    element[0].style.height = `calc(100% - ${amount})`;
  }

  changeWidth(element, amount) {

  }

  link(scope, element, attrs) {
    let checkAndChange = (e, data) => {
      if (element[0].matches(data.selector)) {
        if (attrs.resizableListener === 'height') {
          ResizableListener.instance.changeHeight(element, data.amount);
        } else {
          ResizableListener.instance.changeWidth(element, data.amount);
        }
      }
    };

    scope.$on('Resizable.CHANGE', checkAndChange);
  }

  static directiveFactory() {
    ResizableListener.instance = new ResizableListener();
    return ResizableListener.instance;
  }
}

exports.inject = function(module) {
  module.directive('resizableListener', ResizableListener.directiveFactory);
};
