/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

let ParametersListConstructor = require('./common-parameters/common-parameters-list.js');
let NumericParameterConstructor = require('./common-parameters/common-numeric-parameter.js');
let StringParameterConstructor = require('./common-parameters/common-string-parameter.js');
let BooleanParameterConstructor = require('./common-parameters/common-boolean-parameter.js');
let SnippetParameterConstructor = require('./common-parameters/common-snippet-parameter.js');
let SingleChoiceParameterConstructor = require('./common-parameters/common-choice-parameter/common-single-choice-parameter.js');
let MultipleChoiceParameterConstructor = require('./common-parameters/common-choice-parameter/common-multiple-choice-parameter.js');
let SelectorParameterConstructor = require('./common-parameters/common-selector/common-selector-parameter.js');
let MultiplierParameterConstructor = require('./common-parameters/common-multiplier-parameter.js');

/*
 * (API parameter's type value) => (constructor)
 */
let parameterConstructors = {
  'numeric': NumericParameterConstructor,
  'string': StringParameterConstructor,
  'boolean': BooleanParameterConstructor,
  'snippet': SnippetParameterConstructor,
  'choice': SingleChoiceParameterConstructor,
  'multipleChoice': MultipleChoiceParameterConstructor,
  'selector': SelectorParameterConstructor,
  'multiplier': MultiplierParameterConstructor
};

let ParameterFactory = {
  createParametersList(paramValues, paramSchemas) {
    let parametersList = {};

    for (let paramName in paramSchemas) {
      let paramSchema = paramSchemas[paramName];
      let paramValue = paramValues[paramName];
      let options = {
        'name': paramName,
        'value': paramValue,
        'schema': paramSchema
      };

      switch (paramSchema.type) {
        case 'choice':
        case 'multipleChoice':
          options.possibleChoicesList = {};
          for (let choiceName in options.schema.values) {
            let choiceParamValues = (options.value || {})[choiceName] || {};
            let choiceParamSchema = options.schema.values[choiceName];

            options.possibleChoicesList[choiceName] = ParameterFactory.createParametersList(
              choiceParamValues,
              choiceParamSchema
            );
          }

          break;
        case 'multiplier':
          options.parametersLists = [];
          paramValue = paramValue || [];
          for (let i = 0; i < paramValue.length; ++i) {
            let nestedParametersList = ParameterFactory.createParametersList(
              paramValue[i],
              options.schema.values
            );

            options.parametersLists.push(nestedParametersList);
          }

          break;
      }

      if (parameterConstructors[paramSchema.type]) {
        let Constructor = parameterConstructors[paramSchema.type];
        if (!_.isUndefined(Constructor)) {
          parametersList[paramName] = new Constructor(options);
        }
      }
    }

    return new ParametersListConstructor({ parameters: parametersList });
  }
};

module.exports = ParameterFactory;
