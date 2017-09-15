# Attributes panel

The project concerns the nodes' parameters panel.

### Dependencies

Front-end:

- AngularJS
- lodash
- bootstrap
- font-awesome
- animate.css
- jQuery
- INSPINIA Bootstrap theme.
- switchery
- ng-switchery

- deepsense.node-parameters
- deepsense.loading-spinner

### API

COMPONENT MODULE `deepsense.attributes-panel`

COMPONENT DIRECTIVE `<deepsense-operation-attributes>`

EXAMPLE OF USAGE

    <deepsense-operation-attributes
      name="{String}"
      parameters="{Object}"
      description="{String}">
    </deepsense-operation-attributes>

ARGUMENTS

- node | {Object} | object with parameters of node, where
  - `.parameters` | {Object} | param is used in order to show list of all possible parameters in this node
  - `.name` | {String} | name of node
  - `.description` | {String} | description of the node, which is shown in the bottom

EVENTS

- AttributePanel.UNSELECT_NODE | `$emit()` | click | No args | Click on close icon on panel

### Development

`gulp start` to start watchers
`gulp` to build

### Version

0.0.6