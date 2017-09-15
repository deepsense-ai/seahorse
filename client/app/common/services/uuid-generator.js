'use strict';

function UUIDGenerator() {

  var that = this;

  that.generateUUIDPart = function generateUUIDPart() {
    return Math.floor((1 + Math.random()) * 0x10000)
      .toString(16)
      .substring(1);
  };

  that.generateUUID = function generateGUID() {
    return (
      that.generateUUIDPart() + that.generateUUIDPart() + '-' +
      that.generateUUIDPart() + '-' +
      that.generateUUIDPart() + '-' +
      that.generateUUIDPart() + '-' +
      that.generateUUIDPart() + that.generateUUIDPart() + that.generateUUIDPart()
    );
  };

  return that;
}

exports.inject = function(module) {
  module.service('UUIDGenerator', UUIDGenerator);
};
