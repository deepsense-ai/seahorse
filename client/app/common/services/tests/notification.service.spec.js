'use strict';

describe('NotificationService', function() {
  var NotificationService;
  var $rootScope;
  var toastr;
  var $log;

  beforeEach(function() {
    var testModule = angular.module('test', ['ui.router', 'toastr']);

    if (typeof Function.prototype.bind !== 'function') {
      Function.prototype.bind = function() {
        var slice = Array.prototype.slice;
        return function(context) {
          var fn = this,
            args = slice.call(arguments, 1);
          if (args.length) {
            return function() {
              return arguments.length ?
                fn.apply(context, args.concat(slice.call(arguments))) :
                fn.apply(context, args);
            };
          }
          return function() {
            return arguments.length ?
              fn.apply(context, arguments) :
              fn.call(context);
          };
        };
      };
    }

    require('./../../../app.run.js')
      .inject(testModule);
    require('./../../../app.config.js')
      .inject(testModule);
    require('./../page.service.js')
      .inject(testModule);
    require('./../notification.service.js')
      .inject(testModule);

    angular.mock.module('test');

    angular.mock.inject(function($injector, _NotificationService_) {
      NotificationService = _NotificationService_;
      $rootScope = $injector.get('$rootScope');
      toastr = $injector.get('toastr');
      $log = $injector.get('$log');
    });
  });

  it('should be defined', function() {
    expect(NotificationService)
      .toBeDefined();
    expect(NotificationService)
      .toEqual(jasmine.any(Object));
  });

  it('should be extended with console log service', function() {
    expect(NotificationService.$log)
      .toBeDefined();
    expect(NotificationService.$log)
      .toEqual($log);
    expect(NotificationService.error)
      .toBeDefined();
    expect(NotificationService.error)
      .toEqual(jasmine.any(Function));
  });

  it('should have constructed with necessary properties', function() {
    expect(NotificationService.$rootScope)
      .toBeDefined();
    expect(NotificationService.$rootScope)
      .toEqual($rootScope); // I AM ROOT
    expect(NotificationService.toastr)
      .toBeDefined();
    expect(NotificationService.toastr)
      .toEqual(toastr);
    expect(NotificationService.staticMessages)
      .toBeDefined();
    expect(NotificationService.staticMessages)
      .toEqual(jasmine.any(Object));
    expect(NotificationService.dynamicMessages)
      .toBeDefined();
    expect(NotificationService.dynamicMessages)
      .toEqual(jasmine.any(Object));
  });

  describe('should have event listeners as static and dynamic messages', function() {
    it('should have static messages', function() {
      var staticMessagesLength = Object.keys(NotificationService.staticMessages)
        .length;
      expect(staticMessagesLength)
        .toEqual(0);
    });

    it('should have dynamic messages', function() {
      var dynamicMessagesLength = Object.keys(NotificationService.dynamicMessages)
        .length;
      expect(dynamicMessagesLength)
        .not.toEqual(0);
    });

    it('should have messages array to store all messages and prevent memory ' +
      'leak',
      function() {
        expect(NotificationService.messages)
          .toEqual(jasmine.any(Array));
        var messagesLength = Object.keys(NotificationService.messages)
          .length;
        expect(messagesLength)
          .toEqual(0);
      });
  });

  describe('should have init events listeners method', function() {
    beforeEach(function() {
      spyOn(NotificationService.$rootScope, '$on');
      spyOn(NotificationService, 'transportEventToShowByName');
    });

    it('should be defined', function() {
      expect(NotificationService.initEventListeners)
        .toBeDefined();
      expect(NotificationService.initEventListeners)
        .toEqual(jasmine.any(Function));
    });

    it('should add all event listeners from static messages', function() {
      NotificationService.initEventListeners();

      for (var eventName in NotificationService.staticMessages) {
        if (NotificationService.staticMessages.hasOwnProperty(eventName)) {
          expect(NotificationService.$rootScope.$on)
            .toHaveBeenCalledWith(eventName, jasmine.any(Function));
        }
      }
    });

    it('should add all event listeners from dynamic messages', function() {
      NotificationService.initEventListeners();

      for (var eventName in NotificationService.dynamicMessages) {
        if (NotificationService.dynamicMessages.hasOwnProperty(eventName)) {
          expect(NotificationService.$rootScope.$on)
            .toHaveBeenCalledWith(eventName, jasmine.any(Function));
        }
      }
    });

    /*it('should fire listeners after event broadcasting', function () {
      var staticEvents = Object.keys(NotificationService.staticMessages);

      NotificationService.initEventListeners();

      $rootScope.$emit(staticEvents[0]);

      expect(NotificationService.transportEventToShowByName)
        .toHaveBeenCalledWith(jasmine.objectContaining({
          name: staticEvents[0]
        }));
    });*/
  });
});
