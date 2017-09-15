/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by: Grzegorz Swatowski
 */

'use strict';

let GenericParameter = require('./common-generic-parameter.js');
let ValidatorFactory = require('./common-validators/common-validator-factory.js');

class CreatorParameter extends GenericParameter {
  constructor(options) {
    super();
    this.name = options.name;
    this.initValue(options.value, options.schema);
    this.schema = options.schema;
    this.validator = ValidatorFactory.createValidator(this.schema.type, this.schema.validator);
  }

  serialize() {
    return this.value;
  }

  validate() {
    return this.validator.validate(this.getValueOrDefault());
  };

}


module.exports = CreatorParameter;
