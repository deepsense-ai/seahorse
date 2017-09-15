'use strict';

/*@ngInject*/
function AttributesSerializedView() {
  return {
    restrict: 'E',
    templateUrl: 'app/common/deepsense-components/deepsense-attributes-panel/attribute-types/attribute-column-selector/attributes-serialized-view/attributes-serialized-view.html',
    link: function (scope) {
      _.assign(scope, {
        getNamesList() {
          let columnListObject = scope.getItemsThisTypeOrDefault('columnList')[0];
          return columnListObject && columnListObject.columns;  // TODO shorten when there is a lot of names
        },
        getIndexList() {
          return scope.getItemsThisTypeOrDefault('indexRange');
        },
        getTypesList() {
          let typesList = scope.getItemsThisTypeOrDefault('typeList')[0];
          if (typesList && typesList.types) {
            return _.any(_.values(typesList.types)) ? typesList.types : null;
          }
        },
        getName() {
          let nameObj = scope.getItemsThisTypeOrDefault('column')[0];
          return nameObj && nameObj.column.name;
        },
        getIndex() {
          let nameObj = scope.getItemsThisTypeOrDefault('index')[0];

          return nameObj && nameObj.firstNum >= 0 && !_.isNull(nameObj.firstNum) ?
            nameObj.firstNum : null;
        },
        isValueEmpty() {
          return (scope.parameter.schema.isSingle || !scope.parameter.excluding) && _.isEmpty(scope.parameter.items);
        },
        getSingleValueOrDefault() {
          if (this.isValueEmpty()) {
            return scope.parameter.defaultItems[0];
          } else {
            return scope.parameter.items[0];
          }
        },
        isExcluding() {
          return scope.parameter.excluding || (this.isValueEmpty() && scope.parameter.defaultExcluding);
        },
        isIncluding() {
          return !this.isExcluding();
        }
      });
    }
  };
}

angular.module('deepsense.attributes-panel').
  directive('attributesSerializedView', AttributesSerializedView);
