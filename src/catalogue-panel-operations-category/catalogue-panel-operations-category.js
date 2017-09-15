namespace.factory('RecursionHelper', ['$compile', function($compile){
  return {
    /**
     * Manually compiles the element, fixing the recursion loop.
     * @param element
     * @param [link] A post-link function, or an object with function(s) registered via pre and post properties.
     * @returns An object containing the linking functions.
     */
    compile: function(element, link){
      // Normalize the link parameter
      if(angular.isFunction(link)){
        link = { post: link };
      }

      // Break the recursion loop by removing the contents
      var contents = element.contents().remove();
      var compiledContents;
      return {
        pre: (link && link.pre) ? link.pre : null,
        /**
         * Compiles and re-adds the contents
         */
        post: function(scope, element){
          // Compile the contents
          if(!compiledContents){
            compiledContents = $compile(contents);
          }
          // Re-add the compiled contents to the element
          compiledContents(scope, function(clone){
            element.append(clone);
          });

          // Call the post-linking function, if any
          if(link && link.post){
            link.post.apply(null, arguments);
          }
        }
      };
    }
  };
}]);

function OperationsCategory(RecursionHelper) {
  return {
    templateUrl: "catalogue-panel-operations-category/catalogue-panel-operations-category.html",
    replace: "true",
    scope: {
      search: '=',
      category: '='
    },
    controllerAs: 'ocCtrl',
    controller: function() {
      var ocCtrl = this;

      ocCtrl.content = {};
      ocCtrl.templateUrl = 'catalogue-panel-operations-category/popoverTemplate.html';

      ocCtrl.selectContentForPopover = function(operation) {
        ocCtrl.content = operation;
      }
    },
    compile: function(element, scope) {
      // Use the compile function from the RecursionHelper,
      // And return the linking function(s) which it returns
      return RecursionHelper.compile(element);
    }
  };
}

OperationsCategory.$inject = ['RecursionHelper'];

namespace.directive("operationsCategory", OperationsCategory);
