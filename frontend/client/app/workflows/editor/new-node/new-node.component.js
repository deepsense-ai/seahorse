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

import NewNodeTemplate from './new-node.html';
import './new-node.less';

const NewNodeComponent = {
  bindings: {
    'categories': '<',
    'containment': '<',
    'data': '<',
    'onDisplayRectChange': '&',
    'onSelect': '&'
  },
  controller: class NewNodeController {
    constructor($element, $timeout) {
      'ngInject';

      this.$element = $element;
      this.$timeout = $timeout;
    }
    $postLink() {
      this.$element.bind('mousedown click', (event) => {
        event.stopPropagation();
      });

      this.$timeout(() => {
        const {top, left, right, bottom} = this.$element[0].getBoundingClientRect();
        const rect = {top, left, right, bottom};
        this.onDisplayRectChange({rect});
      }, 0);
    }
  },
  templateUrl: NewNodeTemplate
};

export default NewNodeComponent;
