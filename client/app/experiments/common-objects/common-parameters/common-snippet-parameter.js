/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

let GenericParameter = require('./common-generic-parameter.js');

function SnippetParameter(options) {
  this.name = options.name;
  this.value = this.initValue(options.value, options.schema);
  this.schema = options.schema;
}

SnippetParameter.prototype = new GenericParameter();
SnippetParameter.prototype.constructor = GenericParameter;

SnippetParameter.prototype.serialize = function () {
  return this.value;
};

module.exports = SnippetParameter;
