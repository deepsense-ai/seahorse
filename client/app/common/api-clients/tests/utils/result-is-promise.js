'use strict';

module.exports = function resultIsPromise ($httpBackend, functionReturningPromise, expectRequest) {
  expectRequest();
  let promise = functionReturningPromise();
  expect(promise).toEqual(jasmine.any(Object));
  expect(promise.then).toEqual(jasmine.any(Function));
  expect(promise.catch).toEqual(jasmine.any(Function));
  $httpBackend.flush();
};
