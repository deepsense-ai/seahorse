'use strict';

let GenericParameter = require('./common-generic-parameter.js');

function SaveToLibraryParameter(options) {
  this.name = options.name;
  this.initValue(options.value, options.schema);
  this.schema = options.schema;
}

SaveToLibraryParameter.prototype = new GenericParameter();
SaveToLibraryParameter.prototype.constructor = GenericParameter;

SaveToLibraryParameter.prototype.serialize = function () {
  return this.value;
};

module.exports = SaveToLibraryParameter;
