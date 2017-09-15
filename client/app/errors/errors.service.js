'use strict';

class ErrorService {
  constructor() {
    let errors = {};
    errors[409] = 'ConflictState';
    errors[404] = 'MissingState';
    errors[-1] = 'MissingState';
    this.errorsTable = errors;
  }

  getErrorState(errorId) {
    return this.errorsTable[errorId];
  }
}

exports.function = ErrorService;

exports.inject = function (module) {
  module.service('ErrorService', ErrorService);
};
