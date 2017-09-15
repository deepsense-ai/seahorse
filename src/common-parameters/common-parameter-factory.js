/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Swatowski
 */

'use strict';

let ParametersListConstructor = require('./common-parameters-list.js');
let NumericParameterConstructor = require('./common-numeric-parameter.js');
let StringParameterConstructor = require('./common-string-parameter.js');
let BooleanParameterConstructor = require('./common-boolean-parameter.js');
let SnippetParameterConstructor = require('./common-snippet-parameter.js');
let SingleChoiceParameterConstructor = require('./common-choice-parameter/common-single-choice-parameter.js');
let MultipleChoiceParameterConstructor = require('./common-choice-parameter/common-multiple-choice-parameter.js');
let SelectorParameterConstructor = require('./common-selector/common-selector-parameter.js');
let MultiplierParameterConstructor = require('./common-multiplier-parameter.js');

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
    let parametersList = [];

    for (let i = 0; i < paramSchemas.length; ++i) {
      let paramSchema = paramSchemas[i];
      let paramName = paramSchema.name;
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
          _.forEach(options.schema.values, (choiceObject) => {
            let choiceName = choiceObject.name;
            let choiceParamValues = (options.value || {})[choiceName] || {};
            let choiceParamSchema = choiceObject.schema;

            options.possibleChoicesList[choiceName] = ParameterFactory.createParametersList(
              choiceParamValues,
              choiceParamSchema
            );
          });

          break;
        case 'multiplier':
          options.parametersLists = [];
          paramValue = paramValue || [];
          _.forEach(paramValue, (multiplier) => {
            let nestedParametersList = ParameterFactory.createParametersList(
              multiplier,
              options.schema.values
            );

            options.parametersLists.push(nestedParametersList)
          });

          break;
      }

      if (parameterConstructors[paramSchema.type]) {
        let Constructor = parameterConstructors[paramSchema.type];
        if (!_.isUndefined(Constructor)) {
          parametersList.push(new Constructor(options));
        }
      }
    }

    return new ParametersListConstructor({ parameters: parametersList });
  }
};

module.exports = ParameterFactory;
