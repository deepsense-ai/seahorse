'use strict';

function ParametersList(options) {
  this.parameters = options.parameters;
}

ParametersList.prototype.serialize = function () {
  return _.reduce(this.parameters, (acc, parameter) => {
    acc[parameter.name] = parameter.serialize();
    return acc;
  }, {});
};

ParametersList.prototype.validate = function() {
  return _.every(_.map(this.parameters, (parameter) => parameter.validate()));
};

module.exports = ParametersList;
