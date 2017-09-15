'use strict';

const OBJECT_TYPE = 'application/seahorseObjects/';

class CopyPasteService {
  /* @njInject */
  constructor($document, $rootScope, NodeCopyPasteVisitorService) {
    _.assign(this, {$document, $rootScope, NodeCopyPasteVisitorService});

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
    if (this.enabled) {
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

exports.inject = function(module) {
  module.service('CopyPasteService', CopyPasteService);
};