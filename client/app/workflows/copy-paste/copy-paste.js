'use strict';

let currentCopiedEntity = null;
let memoriseItems = new Set();

class CopyPasteService {
  /* @njInject */
  constructor($document, $rootScope) {
    this.deps = {
      $document, $rootScope
    };

    this.init();
  }

  triggerEntityPasteEvent(entity) {
    this.deps.$rootScope.$broadcast(`CopyPaste.PASTE.${entity.type}`, entity);
  }

  pasteViaFilter(event) {
    if (memoriseItems.has(currentCopiedEntity)) {
      return currentCopiedEntity.paste(event, currentCopiedEntity.entityToCopy);
    }
  }

  filterToMemorize() {
    let hasBeenMemorized = false;

    memoriseItems.forEach((copyObject) => {
      if (copyObject.filter()) {
        this.memorize(copyObject);
        hasBeenMemorized = true;
      }
    });

    if (!hasBeenMemorized) {
      this.clearMemorize();
    }
  }

  clearMemorize() {
    currentCopiedEntity = null;
  }

  memorize(entity) {
    currentCopiedEntity = entity;
    currentCopiedEntity.entityToCopy = (
      entity.getEntityToCopy || entity.filter
    )();
    this.deps.$rootScope.$broadcast(`CopyPaste.COPY.${entity.type}`, entity);
  }

  copy(event) {
    this.deps.$rootScope.$broadcast('CopyPaste.COPY', event);
    this.filterToMemorize();
  }

  paste(event) {
    this.deps.$rootScope.$broadcast('CopyPaste.PASTE', event);

    if (currentCopiedEntity) {
      this.pasteViaFilter(event).then(
        this.triggerEntityPasteEvent.bind(this, currentCopiedEntity)
      );
    }
  }

  add(copyObject) {
    memoriseItems.add(copyObject);
  }

  init() {
    this.deps.$document.on('copy', this.copy.bind(this));
    this.deps.$document.on('paste', this.paste.bind(this));
  }
}

exports.inject = function(module) {
  module.service('CopyPasteService', CopyPasteService);
};
