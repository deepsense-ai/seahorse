/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

function GenericSelectorItem() {}

GenericSelectorItem.prototype.serialize = () => {
  throw 'serialize method not defined';
};

GenericSelectorItem.prototype.containsField = function(field, index, fieldsCount) {
  throw 'containsField method not defined';
};

GenericSelectorItem.prototype.validate = () => true;

module.exports = GenericSelectorItem;
