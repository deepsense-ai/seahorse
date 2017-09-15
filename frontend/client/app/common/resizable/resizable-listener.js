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
