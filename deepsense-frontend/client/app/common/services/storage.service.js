'use strict';

/* @ngInject */
function StorageService($log) {
  const vm = this;
  const cache = {};
  vm.get = get;
  vm.set =  set;
  vm.remove = remove;
  vm.getRoot = getRoot;

  return vm;

  /**
   * Gets the parsed store object.
   * Cache is added for angular watchers to work properly. Without it, angular would treat each localStore.getImte as
   * new object, because of JSON.parse call.
   * @param {string} root - key of the store
   * @returns {Object}
     */
  function getRoot(root) {
    if (cache[root]) {
      return cache[root];
    }
    let rootStoreString = window.localStorage.getItem(root) || '{}';
    let rootStore;
    try {
      rootStore = JSON.parse(rootStoreString);
    } catch (error) {
      $log.error('Localstorage get error = ' + error);
      $log.error('Localstorage clearing rootStore = ' + root + rootStoreString);
      return {};
    }
    cache[root] = rootStore;
    return rootStore;
  }

  /**
   * Gets the object stored in the store on given key
   * @param {string} root - key of the store
   * @param {string} key - key of element inside the store
   * @returns {*|undefined}
   */
  function get(root, key) {
    return getRoot(root)[key];
  }

  /**
   * Updates the localStorage object with new value in defined rootStore
   * @param {string} root - key of the store
   * @param {string} key - key of element inside the store
   * @param {string} value - value of the element stored in the store on given key
   * @returns {undefined}
   */
  function set(root, key, value) {
    let rootObject = getRoot(root);
    rootObject[key] = value;
    return window.localStorage.setItem(root, JSON.stringify(rootObject));
  }

  /**
   * Removes element stored in the store on key
   * @param {string} root - key of the store
   * @param {string} key - key of element inside the store
   * @returns {undefined}
   */
  function remove(root, key) {
    let rootObject = getRoot(root);
    delete rootObject[key];
    return window.localStorage.setItem(root, JSON.stringify(rootObject));
  }
}


exports.inject = function(module) {
  module.service('StorageService', StorageService);
};
