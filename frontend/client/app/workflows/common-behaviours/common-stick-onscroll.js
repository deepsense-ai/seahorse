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

/* @ngInject */
function StickOnScroll($window, $document, debounce) {
  return {
    restrict: 'A',
    link: function(scope, element, attributes) {
      // also in common-stick-onscroll.less
      var CSS_CLASS_TO_STICK = 'stuck';
      var CSS_CLASS_TO_STICK_TO_LEFT = 'stuckLeft';
      var ANIMATE_SPEED = 150;
      var DEBOUNCE_VALUE = ANIMATE_SPEED;

      var $element = $(element);
      var $stickEndPoint = $(attributes.stickEndPoint);
      var elementAbsolutePositionTop;
      var startOn = attributes.stickOn;
      var isAnimated = typeof attributes.stickAnimate === 'string';
      var $clone;
      var originalWidth = $element.width();

      var calculateCorrectTopOffset = function calculateCorrectTopOffset() {
        // store top mistake because of display: flex
        $element.addClass(CSS_CLASS_TO_STICK);
        var result = $element.offset()
          .top;
        $element.removeClass(CSS_CLASS_TO_STICK);

        return result;
      };

      var topMistake = calculateCorrectTopOffset();

      var mirror = function mirror(hasToBeCreated) {
        if (hasToBeCreated) {
          $clone = $element.clone(true)
            .removeClass(CSS_CLASS_TO_STICK);
          $clone.css({
            'visibility': 'hidden',
            'z-index': '-1'
          });
          $clone.insertBefore($element);
        } else if ($clone) {
          $clone.remove();
        }
      };

      var stick = function stick(viewScroll) {
        elementAbsolutePositionTop = elementAbsolutePositionTop || $element.offset()
          .top;

        if (viewScroll >= elementAbsolutePositionTop && $element.hasClass(CSS_CLASS_TO_STICK) === false) {
          $element.addClass(CSS_CLASS_TO_STICK);
          $element.addClass(CSS_CLASS_TO_STICK_TO_LEFT);
          mirror(true);
          $element.width($clone.width());
        } else if (viewScroll < elementAbsolutePositionTop) {
          mirror(false);
          $element.width(originalWidth);
          $element.removeClass(CSS_CLASS_TO_STICK);
          $element.removeClass(CSS_CLASS_TO_STICK_TO_LEFT);
        }
      };

      var setPosition = function setPosition(viewScroll) {
        var method = 'css';

        if (isAnimated) {
          method = 'animate';
        }

        if (unstick(viewScroll)) {
          return true;
        }

        if ($element.hasClass(CSS_CLASS_TO_STICK)) {
          $element[method]({
            'top': (viewScroll - topMistake) + 'px'
          }, ANIMATE_SPEED, unstick.bind(undefined, viewScroll));
        } else {
          $element[method]({
            'top': 0
          });
        }
      };

      var unstick = function unstick(viewScroll) {
        if ($stickEndPoint.length === 0) {
          return true;
        }

        let stickEndPointAbsolutePositionTop = $stickEndPoint.offset()
          .top - topMistake;

        if (viewScroll < parseInt($element.css('top'), 10)) {
          return false;
        }

        if ((parseInt($element.css('top'), 10) + $element.outerHeight(true)) >= stickEndPointAbsolutePositionTop) {
          // Bottom reached!
          $element.css('top', `${stickEndPointAbsolutePositionTop - $element.outerHeight(true)}px`);
          return true;
        }
      };

      var runListener = function runListener(event) {
        var viewScroll = $document[0].body.scrollTop;

        stick(viewScroll);
        setPosition(viewScroll);
      };

      var init = function init() {
        if (isAnimated) {
          setPosition = debounce(DEBOUNCE_VALUE, setPosition);
        }

        // if there is specified listener
        if (startOn) {
          scope.$on(startOn, runListener);
        } else {
          // It needs to be, in order to have all elements, which affect the position, calculated
          scope.$applyAsync(runListener);
        }

        $window.addEventListener('scroll', runListener);

        scope.$on('$destroy', () => {
          $window.removeEventListener('scroll', runListener);
        });
      };

      init();
    }
  };
}

exports.inject = function(module) {
  module.directive('stickOnScroll', StickOnScroll);
};
