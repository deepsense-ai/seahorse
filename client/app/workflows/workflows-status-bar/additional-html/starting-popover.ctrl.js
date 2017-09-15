'use strict';

/* @ngInject */
class StartingPopoverCtrl {
  constructor(config) {
    this.StartingPopoverKey = config.apiVersion + '-startingPopover';
  }

  isPopoverClosed() {
    const startingPopover = JSON.parse(localStorage.getItem(this.StartingPopoverKey));
    return startingPopover && startingPopover.closed;
  }

  closePopover() {
    localStorage.setItem(this.StartingPopoverKey, JSON.stringify({
      closed: true
    }));
  }

}

exports.inject = function (module) {
  module.controller('StartingPopoverCtrl', StartingPopoverCtrl);
};
