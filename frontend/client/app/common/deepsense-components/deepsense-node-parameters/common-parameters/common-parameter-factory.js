/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
let LoadFromLibraryParameterConstructor = require('./common-load-from-library-parameter.js');
let SaveToLibraryParameterConstructor = require('./common-save-to-library-parameter.js');
let DatasourceReadParameterConstructor = require('./common-datasource-read-parameter.js');
let DatasourceWriteParameterConstructor = require('./common-datasource-write-parameter.js');

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
  'multipleNumeric': MultipleNumericParameterConstructor,
  'loadFromLibrary': LoadFromLibraryParameterConstructor,
  'saveToLibrary': SaveToLibraryParameterConstructor,
  'datasourceIdForRead': DatasourceReadParameterConstructor,
  'datasourceIdForWrite': DatasourceWriteParameterConstructor
};

let ParameterFactory = {

  createParametersList(paramValues, paramSchemas, node, isDynamic = false) {
    let parametersList = [];

    if (!_.isNull(paramSchemas)) {
      for (let i = 0; i < paramSchemas.length; ++i) {
        let paramSchema = paramSchemas[i];
        let paramName = paramSchema.name;
        let paramValue = paramValues[paramName];
        let options = {
          'name': paramName,
          'value': paramValue,
          'schema': paramSchema,
          'isDynamic': isDynamic
        };

        switch (paramSchema.type) {
          case 'choice':
          case 'multipleChoice':
            options.possibleChoicesList = {};
            _.forEach(options.schema.values, (choiceObject) => {
              let choiceName = choiceObject.name;
              let choiceParamValues = (options.value || {})[choiceName] || {};

              let innerParamDefaults = (options.schema.default || {})[choiceName];
              let choiceParamSchemaWithDefaults = choiceObject.schema.slice(0);

              // If there is no user-defined value supported, we set param default values to inferred default values.
              if (_.isUndefined(options.value) || _.isNull(options.value)) {
                _.forEach(innerParamDefaults, function (value, key) {
                  let schemaEntry = _.find(choiceParamSchemaWithDefaults, function (item) {
                    return item.name === key;
                  });
                  schemaEntry.default = value;
                });
              }

              options.possibleChoicesList[choiceName] = ParameterFactory.createParametersList(
                choiceParamValues,
                choiceParamSchemaWithDefaults,
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

              options.parametersLists.push(nestedParametersList);
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
          // no default
        }

        if (parameterConstructors[paramSchema.type]) {
          let Constructor = parameterConstructors[paramSchema.type];
          if (!_.isUndefined(Constructor)) {
            parametersList.push(new Constructor(options, node, this));
          }
        } else {
          /* eslint-disable no-console */
          console.warn('No constructor for param of type ' + paramSchema.type);
          /* eslint-enable no-console */
        }
      }
    }

    return new ParametersListConstructor({ parameters: parametersList });
  }

};

module.exports = ParameterFactory;
