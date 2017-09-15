'use strict';

/*@ngInject*/
function KnownColsSelectorItem() {

  let getSingleItemByType = (scope, type) => {
    // Note that we assume there is at most one ColumnList selection in items
    let items = scope.getItemsThisType(type);
    return (items.length === 0) ? scope.addItem(type) : items[0];
  };

  let linkSingle = (scope) => {
    let item = getSingleItemByType(scope, "column");

    let selected = item.column.name ? {field: {name: item.column.name}} : {};

    scope.$watchCollection('selected', function(newValue) {
      item.column.name = newValue.field ? newValue.field.name : undefined;
    });

    _.assign(scope, {
      selected: selected,
      removeName() {
        delete selected.field;
      }
    });
  };

  let linkMulti = (scope) => {
    let item = getSingleItemByType(scope, "columnList");

    // [{name: a}, {name: b}, {name: c}] --> {a: true, b: true, c: true}
    let selected = _.zipObject(_.map(item.columns, column => [column.name, true]));

    // [{a: true, b: false, c: true}] --> [a, c]
    let selectedNames = function selectedNames() {
      return Object.keys(_.pick(selected, x => x));
    };

    scope.$watchCollection('selected', function(newValue) {
      item.columns = _.map(selectedNames(), name => {
        return {name: name};
      });
    });

    _.assign(scope, {
      selected: selected,
      selectedNames: selectedNames,
      selectAll(visible) {
        _.forEach(visible, field => {
          selected[field.name] = true;
        });
      },
      unselectAll(visible) {
        _.forEach(visible, field => {
          delete selected[field.name];
        });
      },
      removeName(name) {
        delete selected[name];
      }
    });
  };

  return {
    restrict: 'E',
    templateUrl: 'attribute-types/attribute-column-selector/known-cols-selector-item/known-cols-selector-item.html',
    replace: true,
    link: function (scope) {
      return scope.selectorIsSingle() ? linkSingle(scope) : linkMulti(scope);
    }
  };
}

angular.module('deepsense.attributes-panel').
    directive('knownColsSelectorItem', KnownColsSelectorItem);
