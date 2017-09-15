'use strict';

module.exports = function promiseIsResolved ($httpBackend, url, expectedResponse, functionReturningPromise, expectRequest) {
  let success = false;
  let error   = false;
  let responseData = null;

  expectRequest();

  functionReturningPromise().
    then((data) => {
      success = true;
      responseData = data;
    }).
    catch(() => { error = true; });

  $httpBackend.flush();

  expect(success).toBe(true);
  expect(error).toBe(false);
  expect(responseData).toEqual(expectedResponse);
};
