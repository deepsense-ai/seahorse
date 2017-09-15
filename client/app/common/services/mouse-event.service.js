'use strict';

/* @ngInject */
function MouseEvent() {
  // Should we test other Apple OS'es? /Mac|iPod|iPhone|iPad/
  const modKey = /Mac/.test(navigator.platform) ? 'metaKey' : 'ctrlKey';

  var that = this;
  var internal = {};

  internal.getScale = function getScale(element) {
    var match = element.style.transform && element.style.transform.match(/scale\((\d+?(?:\.\d+?)?)\)/);

    if (match && match[1]) {
      return +match[1];
    }

    return 1;
  };

  that.getWindowScroll = function getWindowScroll(event) {
    var supportPageOffset = window.pageXOffset !== undefined;
    var isCSS1Compat = (document.compatMode || '') === 'CSS1Compat';

    return {
      x: supportPageOffset ? window.pageXOffset : isCSS1Compat ? document.documentElement.scrollLeft : document.body.scrollLeft,
      y: supportPageOffset ? window.pageYOffset : isCSS1Compat ? document.documentElement.scrollTop : document.body.scrollTop
    };
  };

  that.getEventOffsetOfElement = function getEventOffsetOfElement(event, element) {
    var scroll = that.getWindowScroll(event);
    var scale = internal.getScale(element);

    return {
      x: Math.round((event.clientX - scroll.x - element.getBoundingClientRect()
          .left) / scale),
      // y: Math.round((event.clientY - scroll.y - element.getBoundingClientRect().top) / scale)
      y: Math.round((event.clientY + scroll.y - $(element)
          .offset()
          .top) / scale)
    };
  };

  that.isModKeyDown = function isModKeyDown(event) {
    return event[modKey];
  };

  return that;
}

exports.function = MouseEvent;

exports.inject = function(module) {
  module.service('MouseEvent', MouseEvent);
};
