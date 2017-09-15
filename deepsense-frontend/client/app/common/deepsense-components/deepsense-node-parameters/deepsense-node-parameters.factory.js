'use strict';

let ParameterFactory = require('./common-parameters/common-parameter-factory.js');

function NodeParameters() {
    return {
        factory: ParameterFactory
    };
}

exports.inject = function (module) {
    module.factory('DeepsenseNodeParameters', NodeParameters);
};
