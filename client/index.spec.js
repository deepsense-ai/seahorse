import angular from 'angular';
import 'angular-mocks';


describe('Dummy suite', () => {
  var module;

  beforeEach(() => {
    module = angular.module('test', []);
    angular.mock.module('test');
  });

  it('Dummy test should pass', () => {
    expect(true).toEqual(true);
  });
});
