/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

let GenericParameter = require('./common-generic-parameter.js');

function CodeSnippetParameter(options) {
  this.name = options.name;
  this.value = this.initValue(options.value, options.schema);
  this.schema = options.schema;
}

CodeSnippetParameter.prototype = new GenericParameter();
CodeSnippetParameter.prototype.constructor = GenericParameter;

CodeSnippetParameter.prototype.serialize = function () {
  return this.value;
};

module.exports = CodeSnippetParameter;
