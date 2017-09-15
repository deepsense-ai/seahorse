/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
'use strict';

var mock   = require('../mockAPI.js');
var stream = require('stream');

describe('Mock test', function() {
  var res = new stream.Stream();
  res.writable = true;

  var result, flag, head;
  beforeEach(function() {
    result = '';
    flag   = false;
  });

  res.write = function (data) {
    data = data || '';
    result += data.toString();
    return true;
  };

  res.end = function(data) {
    data = data || '';
    result += data.toString();
    flag = true;
  };

  res.writeHead = function(status, header) {
    head = {
      status: 404,
      header: header
    };
  };

  it('should serve empty object', function() {
    mock(
      {url: '/empty'},
      res,
      function() {}
    );

    waitsFor(function() { return flag; }, 5000);

    runs(function() {
      expect(JSON.parse(result)).toEqual({});
    });
  });

  it('should serve example object', function() {
    mock(
      {url: '/test'},
      res,
      function() {}
    );

    waitsFor(function() { return flag; }, 5000);

    runs(function() {
      expect(JSON.parse(result)).toEqual({
        mark: 1,
        my: 'words'
      });
    });
  });

  it('should serve long path', function() {
    mock(
      {url: '/path/longer/than/usual'},
      res,
      function() {}
    );

    waitsFor(function() { return flag; }, 5000);

    runs(function() {
      expect(JSON.parse(result)).toEqual({
        'I am the God of': 'Hellfire',
        'And I bring you': 'Fire'
      });
    });
  });

  it('should produce a 404 when required', function() {
    mock(
      {url: '/there/is/no/such/file'},
      res,
      function() {}
    );

    waitsFor(function() { return flag; }, 5000);

    runs(function() {
      expect(result).toEqual('404 - missing (mock data)');
      expect(head).toEqual({
        status: 404,
        header: {'Content-Type':'text/plain'}
      });
    });
  });

});
