'use strict';

let ParametersListConstructor = require('./common-parameters-list.js');
let NumericParameterConstructor = require('./common-numeric-parameter.js');
let StringParameterConstructor = require('./common-string-parameter.js');
let BooleanParameterConstructor = require('./common-boolean-parameter.js');
let CodeSnippetParameterConstructor = require('./common-code-snippet-parameter.js');
let WorkflowParameterConstructor = require('./common-workflow-parameter.js');
let SingleChoiceParameterConstructor = require('./common-choice-parameter/common-single-choice-parameter.js');
let MultipleChoiceParameterConstructor = require('./common-choice-parameter/common-multiple-choice-parameter.js');
let SelectorParameterConstructor = require('./common-selector/common-selector-parameter.js');
let MultiplierParameterConstructor = require('./common-multiplier-parameter.js');
let CreatorParameterConstructor = require('./common-creator-parameter.js');
let PrefixBasedCreatorParameterConstructor = require('./common-prefix-based-creator-parameter.js');
let DynamicParameterConstructor = require('./common-dynamic-parameter.js');

let GridSearchParameterConstructor = require('./common-gridsearch-parameter.js');
let MultipleNumericParameterConstructor = require('./common-multiple-numeric.js');

/*
 * (API parameter's type value) => (constructor)
 */
let parameterConstructors = {
  'numeric': NumericParameterConstructor,
  'string': StringParameterConstructor,
  'boolean': BooleanParameterConstructor,
  'codeSnippet': CodeSnippetParameterConstructor,
  'workflow': WorkflowParameterConstructor,
  'choice': SingleChoiceParameterConstructor,
  'multipleChoice': MultipleChoiceParameterConstructor,
  'selector': SelectorParameterConstructor,
  'multiplier': MultiplierParameterConstructor,
  'creator': CreatorParameterConstructor,
  'prefixBasedCreator': PrefixBasedCreatorParameterConstructor,
  'dynamic': DynamicParameterConstructor,
  'gridSearch': GridSearchParameterConstructor,
  'multipleNumeric': MultipleNumericParameterConstructor
};

let ParameterFactory = {

  createParametersList(paramValues, paramSchemas, node) {
    let parametersList = [];

    if (!_.isNull(paramSchemas)) {
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
                choiceParamSchema,
                node
              );
            });

            break;
          case 'multiplier':
            options.emptyItem = ParameterFactory.createParametersList(
              {},
              options.schema.values,
              node
            );
            options.parametersLists = [];
            paramValue = paramValue || [];
            _.forEach(paramValue, (multiplier) => {
              let nestedParametersList = ParameterFactory.createParametersList(
                multiplier,
                options.schema.values,
                node
              );

              options.parametersLists.push(nestedParametersList)
            });

            break;

          case 'selector':
            if (paramValue && paramValue.hasOwnProperty('excluding')) {
              options.excluding = paramValue.excluding;
            }
            if (paramValue) {
              options.value = paramSchema.isSingle === false ?
                paramValue.selections :
                options.value;
            }
            break;
        }

        if (parameterConstructors[paramSchema.type]) {
          let Constructor = parameterConstructors[paramSchema.type];
          if (!_.isUndefined(Constructor)) {
            parametersList.push(new Constructor(options, node, this));
          }
        } else {
          console.warn("No constructor for param of type " + paramSchema.type);
        }
      }
    }

    return new ParametersListConstructor({ parameters: parametersList });
  }

};

module.exports = ParameterFactory;
