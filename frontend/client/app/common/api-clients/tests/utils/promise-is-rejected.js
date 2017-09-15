'use strict';

module.exports = function promiseIsRejected($httpBackend, mockRequest, functionReturningPromise, expectRequest) {
  let success = false;
  let error = false;

  expectRequest();

  functionReturningPromise()
    .then(() => {
      success = true;
    })
    .catch(() => {
      error = true;
    });

  mockRequest.respond(500, 'Server Error');

  $httpBackend.flush();

  expect(success)
    .toBe(false);
  expect(error)
    .toBe(true);
};
