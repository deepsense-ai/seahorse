'use strict';

import GenericParameter from './common-generic-parameter.js';

function DatasourceWriteParameter(options) {
  this.name = options.name;
  this.initValue(options.value, options.schema);
  this.schema = options.schema;
}

DatasourceWriteParameter.prototype = new GenericParameter();
DatasourceWriteParameter.prototype.constructor = GenericParameter;

DatasourceWriteParameter.prototype.serialize = function () {
  return this.value;
};

export default DatasourceWriteParameter;
