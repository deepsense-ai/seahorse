# deepsense-node-parameters

The project contains all parameter-related objects. The parameters belong to nodes that can be created in experiments.

The project provides with the *deepsense.node-parameters* AngularJS module.

The module consists of the *DeepsenseNodeParameters* AngularJS factory which has a method *factory*. This method creates
different kinds of parameter objects.

### Dependencies

- AngularJS
- lodash

### API

SERVICE

    DeepsenseNodeParameters

EXAMPLE OF USAGE

    DeepsenseNodeParameters.factory.createParametersList(paramValues, paramSchemas)

where:

  * `paramValues` is an object containing parameters' values that the user already set;
  * `paramSchemas` corresponds to the parameters' schema that can be got via the method `/operations/:id`.

RESULT

    The above function returns an object whose keys are parameters' names and the values are the parameters objects.
