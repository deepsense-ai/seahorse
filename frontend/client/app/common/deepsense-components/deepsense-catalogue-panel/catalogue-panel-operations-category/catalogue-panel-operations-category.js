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

import categoryTpl from './catalogue-panel-operations-category.html';
import popoverTpl from './popoverTemplate.html';

/* @ngInject */
function RecursionHelper($compile) {
  return {
    /**
     * Manually compiles the element, fixing the recursion loop.
     * @param element
     * @param [link] A post-link function, or an object with function(s) registered via pre and post properties.
     * @returns An object containing the linking functions.
     */
    compile: function(element, link) {
      // Normalize the link parameter
      if(angular.isFunction(link)) {
        link = { post: link };
      }

      // Break the recursion loop by removing the contents
      var contents = element.contents().remove();
      var compiledContents;
      return {
        pre: link && link.pre ? link.pre : null,
        /**
         * Compiles and re-adds the contents
         */
        post: function(scope, element) {
          // Compile the contents
          if(!compiledContents) {
            compiledContents = $compile(contents);
          }
          // Re-add the compiled contents to the element
          compiledContents(scope, function(clone) {
            element.append(clone);
          });

          // Call the post-linking function, if any
          if(link && link.post) {
            link.post.apply(null, arguments);
          }
        }
      };
    }
  };
}


/* @ngInject */
function OperationsCategory(RecursionHelper, $log) {
  return {
    templateUrl: categoryTpl,
    replace: true,
    scope: {
      search: '=',
      category: '=',
      isRunning: '='
    },
    controllerAs: 'ocCtrl',
    controller: ['$scope', function($scope) {
      var ocCtrl = this;

      ocCtrl.templateUrl = popoverTpl;

      ocCtrl.selectContentForPopover = function(operation) {
        ocCtrl.content = operation;
      };

      ocCtrl.showCategory = function (category) {
        $log.log('showCategory ', category);
        var canShow = false;
        category.items.forEach(function (item) {
          if (item.name.toLowerCase().indexOf($scope.search) > -1) {
            canShow = true;
          }
        });
        if (category.catalog.length) {
          canShow = true;
        }

        return canShow;
      };

    }],
    compile: function(element, scope) {
      // Use the compile function from the RecursionHelper,
      // And return the linking function(s) which it returns
      return RecursionHelper.compile(element);
    }
  };
}
angular.module('deepsense-catalogue-panel')
  .directive('operationsCategory', OperationsCategory)
  .factory('RecursionHelper', RecursionHelper);

