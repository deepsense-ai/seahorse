# Catalogue panel

Front-end dependencies:

- angular
- bootstrap
- font-awesome
- jquery
- malihu-custom-scrollbar-plugin

**NOTE!** Operation items are draggable. The following attributes are set for each item:

    draggable="true"
    draggable-type="exact"
    draggable-exact-type="graphNode">

### API

COMPONENT MODULE `deepsense-catalogue-panel`

COMPONENT DIRECTIVE `<operation-catalogue>`

EXAMPLE OF USAGE

    <operation-catalogue
      catalog-collection="::collection">
    </operation-catalogue>

ARGUMENTS

  - catalog-collection | {Array} | Set of operations and their titles
    - `.items` | {Array} | Set of operations
    - `.catalog` | {Array} | Subset of operations within some operation title
    - `.id` | {String} | ID of operation
    - `.name` | {String} | Name of operation
    - `.icon` | {String} | CSS class name of font-awesome icon for one operation

### Development

`gulp start` to start browsersync and watchers
`gulp` to build