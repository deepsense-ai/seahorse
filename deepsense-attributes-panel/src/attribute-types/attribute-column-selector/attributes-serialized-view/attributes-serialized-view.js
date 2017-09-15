'use strict';

/*@ngInject*/
function AttributesSerializedView() {
  return {
    restrict: 'E',
    templateUrl: 'attribute-types/attribute-column-selector/attributes-serialized-view/attributes-serialized-view.html',
    replace: true,
    link: function (scope) {
      _.assign(scope, {
        getNamesList() {
          let columnListObject = scope.getItemsThisType('columnList')[0];
          return columnListObject && columnListObject.columns;  // TODO shorten when there is a lot of names
        },
        getIndexList() {
          return scope.getItemsThisType('indexRange');
        },
        getTypesList() {
          let typesList = scope.getItemsThisType('typeList')[0];
          if (typesList && typesList.types) {
            return _.any(_.values(typesList.types)) ? typesList.types : null;
          }
        },
        getName() {
          let nameObj = scope.getItemsThisType('column')[0];
          return nameObj && nameObj.column.name;
        },
        getIndex() {
          let nameObj = scope.getItemsThisType('index')[0];

          return nameObj && nameObj.firstNum >= 0 && !_.isNull(nameObj.firstNum) ?
            nameObj.firstNum : null;
        }
      });
    }
  };
}

angular.module('deepsense.attributes-panel').
  directive('attributesSerializedView', AttributesSerializedView);
