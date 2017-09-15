'use strict';

/* @ngInject */
function UserService($cookies) {

  this.getSeahorseUser = () => {
    return $cookies.getObject('seahorse_user');
  };

}

exports.function = UserService;

exports.inject = function (module) {
  module.service('UserService', UserService);
};
