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
