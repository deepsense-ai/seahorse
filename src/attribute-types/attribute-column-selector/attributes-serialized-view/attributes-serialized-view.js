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
          return columnListObject && columnListObject.columns;
        },
        getIndexList() {
          return scope.getItemsThisType('indexRange');
        },
        getTypesList() {
          let typesList = scope.getItemsThisType('typeList')[0];
          if (typesList && typesList.types) {
            return _.any(_.values(typesList.types)) ? typesList.types : null;
          }
        }
      });
    }
  };
}

angular.module('deepsense.attributes-panel').
  directive('attributesSerializedView', AttributesSerializedView);