/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by: Grzegorz Swatowski
 */

'use strict';

let GenericParameter = require('./common-generic-parameter.js');

class CreatorParameter extends GenericParameter {
  constructor(options) {
    super();
    this.name = options.name;
    this.value = this.initValue(options.value, options.schema);
    this.schema = options.schema;
  }

  serialize() {
    return this.value;
  }
}

module.exports = CreatorParameter;
