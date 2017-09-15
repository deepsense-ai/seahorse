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
- angular-xeditable

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

- AttributesPanel.UPDATED | `$broadcast()` | No args | The event is broadcasted if any parameter has changed its value

- AttributesPanel.OPEN_INNER_WORKFLOW | `$broadcast()` | {'workflowId': '<uuid>', 'nodeId': '<uuid>'} | The event is broadcasted after an "Open inner workflow" button is clicked.

### Additional directives are exported

COMPONENT MODULE `deepsense.attributes-panel`

COMPONENT DIRECTIVE NAME `minValue`

COMPONENT DIRECTIVE `min-value`

EXAMPLE OF USAGE

    <input type="number" min-value="[{Number}]" />

ARGUMENTS

- *min-value (optional) | {Number}  | minimum possible value of an input

COMPONENT MODULE `deepsense.attributes-panel`

COMPONENT DIRECTIVE NAME `labelToFirstUnlabeled`

COMPONENT DIRECTIVE `label-to-first-unlabeled`

EXAMPLE OF USAGE

    <div label-to-first-unlabeled>
      <label>
        Label
      </label>
    </div>
    <div>
      <input type="text" />
    </div>

DESCRIPTION

Add unique ID to first next input element

COMPONENT MODULE `deepsense.attributes-panel`

COMPONENT DIRECTIVE NAME `timeDiff`

COMPONENT DIRECTIVE `time-diff`

EXAMPLE OF USAGE

    <time-diff start="{{angular binding}}" end="{{angular binding}}"></time-diff>

ARGUMENTS

- *start | {Date|Angular binding} | started time
- *end | {Date|Angular binding} | ended time

DESCRIPTION

Shows difference between start and end time

### Additional directives:

- `<... click-listener ...>`
- `<... input-checkbox-change-listener ...>`
- `<... input-text-change-listener ...>`
- `<... select-change-listener ...>`

They listen on UI components' values' changes. If any change appears, the `AttributesPanel.UPDATED` event is triggered.
The event is also triggered when:

- an user changes any value in the column selector panel;
- an user changes any node's custom name;
- an user deletes a group in the multiplier parameter.

### Development

`gulp start` to start watchers
`gulp` to build
