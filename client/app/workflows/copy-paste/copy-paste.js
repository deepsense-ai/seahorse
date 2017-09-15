'use strict';

class CopyPasteService {

  /* @njInject */
  constructor($document, $rootScope) {
    this.$document = $document;
    this.$rootScope = $rootScope;

    this.copyPasteVisitors = [];

    this.init();
  }

  registerCopyPasteVisitor(copyPasteVisitor) {
    this.copyPasteVisitors.push(copyPasteVisitor);
  }

  init() {
    this.$document.on('copy', this._copy.bind(this));
    this.$document.on('paste', this._paste.bind(this));
  }

  _copy(event) {
    this.$rootScope.$broadcast('CopyPaste.COPY', event);

    this.copyPasteVisitors.forEach((copyPasteVisitor) => {
      let dataType = 'application/seahorseObjects/' + copyPasteVisitor.getType();

      if (copyPasteVisitor.isFocused()) {
        if (copyPasteVisitor.isThereAnythingToCopy()) {
          let data = copyPasteVisitor.getSerializedDataToCopy();
          event.clipboardData.setData(dataType, data);
        } else {
          event.clipboardData.clearData(dataType);
        }

        // Needed, so clipboard data is not overriden by default handlers.
        event.preventDefault();
      }

    });
  }

  _paste(event) {
    this.$rootScope.$broadcast('CopyPaste.PASTE', event);

    this.copyPasteVisitors.forEach((copyPasteVisitor) => {
      let isPasteTargetFocused = copyPasteVisitor.isFocused();

      let dataType = 'application/seahorseObjects/' + copyPasteVisitor.getType();
      let isThereSomeData = event.clipboardData.getData(dataType);

      let shouldBePasted = isPasteTargetFocused && isThereSomeData;
      if (shouldBePasted) {
        let serializedData = event.clipboardData.getData(dataType);
        copyPasteVisitor.pasteUsingSerializedData(serializedData);
      }
    });
  }

}

exports.inject = function(module) {
  module.service('CopyPasteService', CopyPasteService);
};
