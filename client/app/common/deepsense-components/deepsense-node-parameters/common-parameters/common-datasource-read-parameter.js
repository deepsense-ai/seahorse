'use strict';

import GenericParameter from './common-generic-parameter.js';

function DatasourceReadParameter(options) {
  this.name = options.name;
  this.initValue(options.value, options.schema);
  this.schema = options.schema;
}

DatasourceReadParameter.prototype = new GenericParameter();
DatasourceReadParameter.prototype.constructor = GenericParameter;

DatasourceReadParameter.prototype.serialize = function () {
  return this.value;
};

export default DatasourceReadParameter;
