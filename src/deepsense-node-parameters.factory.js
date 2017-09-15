/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by Grzegorz Swatowski on 24.06.15.
 */

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
