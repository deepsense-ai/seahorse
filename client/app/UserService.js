'use strict';

/* @ngInject */
function UserService($cookies, $interval) {
  let user = $cookies.getObject('seahorse_user');

  $interval(() => {
     user = $cookies.getObject('seahorse_user');
  }, 1000);

  this.getSeahorseUser = () => {
    return user;
  };

}

exports.function = UserService;

exports.inject = function (module) {
  module.service('UserService', UserService);
};
