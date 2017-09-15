'use strict';

class UUIDGenerator {

  generateUUIDPart() {
    return Math.floor((1 + Math.random()) * 0x10000)
      .toString(16)
      .substring(1);
  }

  generateUUID() {
    return (
      this.generateUUIDPart() + this.generateUUIDPart() + '-' +
      this.generateUUIDPart() + '-' +
      this.generateUUIDPart() + '-' +
      this.generateUUIDPart() + '-' +
      this.generateUUIDPart() + this.generateUUIDPart() + this.generateUUIDPart()
    );
  }
}

exports.inject = function(module) {
  module.service('UUIDGenerator', UUIDGenerator);
};
