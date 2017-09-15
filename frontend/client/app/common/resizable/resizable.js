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

let DEPS = new WeakMap();
let template = _.template('<aside class="o-resizable o-resizable--' +
  '<%= position %> no-selection <% if (invisible) { %> o-resizable--invisible' +
  '<% } %>"></aside>');

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
          diff.x *= -1;
          amount = width + diff.x;
          element[0].style.width = `${amount}px`;
          triggerEvent(amount);
          break;

        case 'top':
          diff.y *= -1;
          amount = height + diff.y;
          if (amount < minAndStart) {
            amount = minAndStart;
          }
          element[0].style.height = `${amount}px`;
          triggerEvent(amount);
          break;
        // no default
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
            element[0].style.height = '25px';
            triggerEvent(25);
          }
        } catch (e) {
          /* eslint-disable no-console */
          console.log(e);
          /* eslint-enable no-console */
        }
      }
    }

    function saveDimensions() {
      if (attrs.resizablePanelName) {
        if (attrs.resizablePosition === 'left' && width) {
          localStorage.setItem(attrs.resizablePanelName, JSON.stringify({
            width: width
          }));
        } else if (attrs.resizablePosition === 'top' && height && height > 50) {
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
        width = parseInt(element[0].style.width, 10);
        height = parseInt(element[0].style.height, 10);
        renderDimensions();
        deps.$document.off('mousemove', move);
        body.removeClass('no-selection');
        saveDimensions();
      });

      scope.$on('Resizable.FIT', (e, data) => {
        deps.$rootScope.$applyAsync(() => {
          if (data.name === 'height' && $(element[0]).is(data.selector)) {
            element[0].style[data.name] = data.amount;
            height = parseInt(element[0].style.height, 10);
          }
        });
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
