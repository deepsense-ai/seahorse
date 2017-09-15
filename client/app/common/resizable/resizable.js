'use strict';

let DEPS = new WeakMap();
let template = _.template(`<aside class="o-resizable o-resizable--\
<%= position %> no-selection <% if (invisible) { %> o-resizable--invisible\
<% } %>"></aside>`);

class Resizable {
  constructor() {
    this.restrict = 'A';
  }

  link(scope, element, attrs) {
    let width;
    let height;
    let deps = DEPS.get(Resizable.instance);
    let body = deps.$document.find('body');
    let resizeElement;
    let startPoint = {};
    let minAndStart = Number(attrs.resizableMinStart);

    function move(e) {
      let diff = {
        x: e.clientX - startPoint.x,
        y: e.clientY - startPoint.y
      };
      let amount;

      switch (attrs.resizablePosition) {
        case 'left':
          diff.x = diff.x - diff.x * 2;
          amount = width + diff.x;
          element[0].style.width = `${amount}px`;
          triggerEvent(amount);
          break;

        case 'top':
          diff.y = diff.y - diff.y * 2;
          amount = height + diff.y;
          if (amount < minAndStart) {
            amount = minAndStart;
          }
          element[0].style.height = `${amount}px`;
          triggerEvent(amount);
          break;
      }
    }

    function triggerEvent(amount) {
      if (attrs.resizableViaListener) {
        deps.$rootScope.$broadcast('Resizable.CHANGE', {
          selector: attrs.resizableViaListener,
          amount
        });
      }
    }

    function loadDimensions() {
      if (attrs.resizablePanelName && localStorage[attrs.resizablePanelName]) {
        try {
          let dimensions = JSON.parse(localStorage.getItem(attrs.resizablePanelName));
          if (dimensions.width) {
            element[0].style.width = `${dimensions.width}px`;
            triggerEvent(dimensions.width);
          }
          if (dimensions.height) {
            element[0].style.height = `${dimensions.height}px`;
            triggerEvent(dimensions.height);
          }
        } catch (e) {
          console.log(e);
        }
      }
    }

    function saveDimensions() {
      if (attrs.resizablePanelName) {
        if (attrs.resizablePosition === 'left' && width) {
          localStorage.setItem(attrs.resizablePanelName, JSON.stringify({
            width: width
          }));
        } else if (attrs.resizablePosition === 'top' && height) {
          localStorage.setItem(attrs.resizablePanelName, JSON.stringify({
            height: height
          }));
        }
      }
    }

    function rest() {
      element.append(template({
        position: attrs.resizablePosition || 'left',
        invisible: attrs.resizableInvisible === 'true'
      }));

      resizeElement = $(element).find('.o-resizable');

      resizeElement.on('mousedown', (e) => {
        startPoint.x = e.clientX;
        startPoint.y = e.clientY;
        deps.$document.on('mousemove', move);
        body.addClass('no-selection');
      });

      deps.$document.on('mouseup', (e) => {
        startPoint.x = e.clientX;
        startPoint.y = e.clientY;
        width = parseInt(element[0].style.width);
        height = parseInt(element[0].style.height);
        renderDimensions();
        deps.$document.off('mousemove', move);
        body.removeClass('no-selection');
        saveDimensions();
      });

      scope.$on('Resizable.FIT', (e, data) => {
        if (data.name === 'height') {
          element[0].style[data.name] = data.amount;
          height = parseInt(element[0].style.height);
        }
      });

      resizeElement.css(attrs.resizablePosition, '+=' + attrs.resizableAddShift);
    }

    function renderDimensions() {
      if (!width) {
        width = jQuery(element[0]).outerWidth(true);
      }

      if (!height) {
        height = minAndStart || jQuery(element[0]).outerHeight(true);
      }
    }

    function init() {
      loadDimensions();
      renderDimensions();
      rest();
    }

    init();
  }

  static directiveFactory($rootScope, $document, $compile) {
    Resizable.instance = new Resizable();
    DEPS.set(Resizable.instance, {
      $rootScope, $document, $compile
    });
    return Resizable.instance;
  }
}

Resizable.directiveFactory.$inject = ['$rootScope', '$document', '$compile'];

exports.inject = function(module) {
  module.directive('resizable', Resizable.directiveFactory);
};
