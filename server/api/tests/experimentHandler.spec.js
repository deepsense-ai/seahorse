/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Piotr Zarówny
 */
'use strict';


describe('Experiment handler', () => {
  var Q = require('q'),
      cloneObject = source => JSON.parse(JSON.stringify(source)),
      mockData = {
        'experiment': {
          'graph': {
            'nodes': {
              '101': {
                'text': 'value'
              },
              '102': {
                'text': 'other'
              }
            }
          },
        }
      },
      mockConfig = {
        'localDB': {
          'collections': {
            'experiment': {
              'findOne': () => {
                return mockConfig.localDB.collections.experiment;
              },
              'where': (params) => {
                let deferred = Q.defer();
                setTimeout(() => {
                  if (params.id === '11') {
                    deferred.resolve({
                      'nodes': {
                        '101': {
                          'extended': true
                        },
                        '111': {
                          'other': false
                        }
                      }
                    });
                  } else {
                    deferred.reject();
                  }
                }, 50);

                return deferred.promise;
              }
            }
          }
        },
        'resources': {
          'experiments': {},
          'others': {}
        }
      },
      config = require('./../experimentHandler.js')(mockConfig);

  it('should attach it self to experiment config', () => {
    expect(config.resources.experiments.handler).not.toBeUndefined();
    expect(config.resources.experiments.handler).toEqual(jasmine.any(Function));

    expect(config.resources.others.handler).toBeUndefined();
  });

  it('should return promise for handled request', () => {
    let handler = config.resources.experiments.handler,
        mockRequest = {
          'resourcePath': 'other',
          'method': 'OTHER',
          'body': {}
        },
        promise = handler(cloneObject(mockData), mockRequest);

    expect(promise).toEqual(jasmine.any(Object));
    expect(promise.then).toEqual(jasmine.any(Function));
  });

  it('should resolve promise for not handled cases', () => {
    let handler = config.resources.experiments.handler,
        mockRequest = {
          'resourcePath': 'other',
          'method': 'OTHER',
          'body': {}
        },
        success = false,
        error = false,
        responseData;

    runs(() => {
      handler(cloneObject(mockData), mockRequest).then((data) => {
        success = true;
        error = false;
        responseData = data;
      }).fail(() => {
        success = false;
        error = true;
      });
    });

    waitsFor(() => success || error, 1000);

    runs(() => {
      expect(success).toBe(true);
      expect(error).toBe(false);
      expect(responseData).toEqual(mockData);
    });
  });

  describe('handles GET requests and', () => {
    let handler = config.resources.experiments.handler,
        mockRequest = {
          'resourcePath': 'other',
          'method': 'GET',
          'body': {}
        },
        success = false,
        error = false,
        responseData;

    beforeEach(() => {
      success = false;
      error = false;
    });

    it('should resolve promise for known resource', () => {
      mockRequest.resourcePath = '11';

      runs(() => {
        handler(cloneObject(mockData), mockRequest).then((data) => {
          success = true;
          error = false;
          responseData = data;
        }).fail(() => {
          success = false;
          error = true;
        });
      });

      waitsFor(() => success || error, 1000);

      runs(() => {
        expect(success).toBe(true);
        expect(error).toBe(false);
        expect(responseData).not.toEqual(mockData);
        expect(responseData.experiment.graph.nodes['101'].ui).not.toBeUndefined();
        expect(responseData.experiment.graph.nodes['101'].ui.extended).toBe(true);
        expect(responseData.experiment.graph.nodes['101'].ui).not.toBeUndefined();
        expect(responseData.experiment.graph.nodes['102'].ui).toBeUndefined();
      });
    });

    it('should reject promise for unknown resource', () => {
      mockRequest.resourcePath = '12';

      runs(() => {
        handler(cloneObject(mockData), mockRequest).then((data) => {
          success = true;
          error = false;
          responseData = data;
        }).fail(() => {
          success = false;
          error = true;
        });
      });

      waitsFor(() => success || error, 1000);

      runs(() => {
        expect(success).toBe(false);
        expect(error).toBe(true);
      });
    });
  });

});
