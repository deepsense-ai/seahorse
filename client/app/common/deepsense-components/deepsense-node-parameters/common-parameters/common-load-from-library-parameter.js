'use strict';

let GenericParameter = require('./common-generic-parameter.js');

function LoadFromLibraryParameter(options) {
  this.name = options.name;
  this.initValue(options.value, options.schema);
  this.schema = options.schema;
}

LoadFromLibraryParameter.prototype = new GenericParameter();
LoadFromLibraryParameter.prototype.constructor = GenericParameter;

LoadFromLibraryParameter.prototype.serialize = function () {
  return this.value;
};

module.exports = LoadFromLibraryParameter;
