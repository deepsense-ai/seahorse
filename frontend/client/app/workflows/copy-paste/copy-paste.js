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

const OBJECT_TYPE = 'application/seahorseObjects/';

class CopyPasteService {
  /* @njInject */
  constructor($document, $rootScope, NodeCopyPasteVisitorService, WorkflowService, UserService) {
    _.assign(this, {$document, $rootScope, NodeCopyPasteVisitorService, WorkflowService, UserService});

    this.enabled = true;
    this.init();
  }

  init() {
    this.$document.on('copy', this._copy.bind(this));
    this.$document.on('paste', this._paste.bind(this));
  }

  setEnabled(enabled) {
    this.enabled = enabled;
  }

  _copy(event) {
    if (this.enabled) {
      const dataType = `${OBJECT_TYPE}${this.NodeCopyPasteVisitorService.getType()}`;
      const isPasteTargetFocused = this.NodeCopyPasteVisitorService.isFocused();

      if (isPasteTargetFocused) {
        if (this.NodeCopyPasteVisitorService.isThereAnythingToCopy()) {
          const data = this.NodeCopyPasteVisitorService.getSerializedDataToCopy();
          event.clipboardData.setData(dataType, data);
        } else {
          event.clipboardData.clearData(dataType);
        }
        event.preventDefault(); // Needed, so clipboard data is not overriden by default handlers.
      }
    }
  }

  _paste(event) {
    const isOwner = this.WorkflowService.getCurrentWorkflow().owner.id === this.UserService.getSeahorseUser().id;
    if (this.enabled && isOwner) {
      const isPasteTargetFocused = this.NodeCopyPasteVisitorService.isFocused();
      const dataType = `${OBJECT_TYPE}${this.NodeCopyPasteVisitorService.getType()}`;
      const serializedData = event.clipboardData.getData(dataType);
      const shouldBePasted = isPasteTargetFocused && serializedData;

      if (shouldBePasted) {
        this.NodeCopyPasteVisitorService.pasteUsingSerializedData(serializedData);
      }
    }
  }
}

exports.inject = function (module) {
  module.service('CopyPasteService', CopyPasteService);
};
