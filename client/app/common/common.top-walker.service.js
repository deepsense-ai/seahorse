/**
 * Created by Oleksandr Tserkovnyi on 07.05.15.
 */

'use strict';

function TopWalkerService() {
  var that = this;

  /**
   * Returns the closest DOM node starting from itself
   *
   * @example walk(td, function (node) { return node.tagName === 'TR'; }, table) // will return parent <tr> element.
   *
   * @param {element} node
   * @param {function} testFunc
   * @param {element} lastParent
   *
   * @returns {element|undefined}
   */
  that.walk = function walk (node, testFunc, lastParent) {
    while ( node && node !== lastParent ) {
      if ( testFunc(node) ) {
        return node;
      }
      node = node.parentNode;
    }
  };

  return that;
}

exports.function = TopWalkerService;

exports.inject = function (module) {
  module.service('TopWalkerService', TopWalkerService);
};
