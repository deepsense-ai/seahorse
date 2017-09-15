/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by Oleksandr Tserkovnyi on 03.06.15.
 */

'use strict';

/* @ngInject */
function MouseEvent() {
  var that = this;
  var internal = {};

  internal.getScale = function getScale(element) {
    var match = element.style.transform && element.style.transform.match(/scale\((\d+?(?:\.\d+?)?)\)/);

    if (match && match[1]) {
      return +match[1];
    }

    return 1;
  };

  /**
   * Implements Window.scrollY, Window.scrollX
   * https://developer.mozilla.org/en-US/docs/Web/API/Window/scrollY
   * https://developer.mozilla.org/en-US/docs/Web/API/Window/scrollX
   *
   * @param {Event} event
   * @returns {{x: number, y: number}}
   */
  that.getWindowScroll = function getWindowScroll(event) {
    var supportPageOffset = window.pageXOffset !== undefined;
    var isCSS1Compat = ((document.compatMode || '') === 'CSS1Compat');

    return {
      x: supportPageOffset ? window.pageXOffset : isCSS1Compat ? document.documentElement.scrollLeft : document.body.scrollLeft,
      y: supportPageOffset ? window.pageYOffset : isCSS1Compat ? document.documentElement.scrollTop : document.body.scrollTop
    };
  };

  /**
   * Implements MouseEvent.offsetX, MouseEvent.offsetY
   * https://developer.mozilla.org/en-US/docs/Web/API/MouseEvent/offsetX
   * https://developer.mozilla.org/en-US/docs/Web/API/MouseEvent/offsetY
   *
   * @param {Event} event
   * @param {HTMLElement} element
   * @returns {{x: number, y: number}}
   */
  that.getEventOffsetOfElement = function getEventOffsetOfElement(event, element) {
    var scroll = that.getWindowScroll(event);
    var scale = internal.getScale(element);

    return {
      x: Math.round((event.clientX - scroll.x - element.getBoundingClientRect().left) / scale),
      // y: Math.round((event.clientY - scroll.y - element.getBoundingClientRect().top) / scale)
      y: Math.round((event.clientY + scroll.y - $(element).offset().top) / scale)
    };
  };

  return that;
}

exports.function = MouseEvent;

exports.inject = function (module) {
  module.service('MouseEvent', MouseEvent);
};
