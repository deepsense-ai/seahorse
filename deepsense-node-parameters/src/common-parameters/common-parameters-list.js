'use strict';

function ParametersList(options) {
  this.parameters = options.parameters;
}

ParametersList.prototype.serialize = function () {
  return _.reduce(this.parameters, (acc, parameter) => {
    let serializedParameter = parameter.serialize();
    acc[parameter.name] = serializedParameter;
    return acc;
  }, {});
};

ParametersList.prototype.validate = function() {
  return _.every(_.map(this.parameters, (parameter) => parameter.validate()));
};

ParametersList.prototype.refresh = function(node) {
  _.forEach(this.parameters, param => {
    param.refresh(node);
  });
};

module.exports = ParametersList;
