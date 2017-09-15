# Cycle analyser

The component is responsible for detecting cycles in a graph consisted of operation nodes.

### Dependencies

- AngularJS
- lodash
- deepsense-graph-model

### API

The component provides with the Angular factory `DeepsenseCycleAnalyser` that has a method `cycleExists`.
The method takes an experiment object which is an instance of the Experiment class declared
in the `deepsense-graph-model` component.

