'use strict';

function TimeService() {
  this.getVerboseDateDiff = (date) => {
    let nowM = moment();
    let dateM = moment(date);
    let diff = nowM.diff(dateM);
    let diffSeconds = Math.floor(diff / 1000);
    let diffMinutes = Math.floor(diffSeconds / 60);
    let diffHours = Math.floor(diffMinutes / 60);
    let diffDays = Math.floor(diffHours / 24);

    if (diffSeconds < 60) {
      return `less than a minute`;
    } else if (diffMinutes < 60) {
      return diffMinutes === 1 ? `a minute` : `${diffMinutes} minutes`;
    } else if (diffHours < 24) {
      return diffHours === 1 ? `an hour` : `${diffHours} hours`;
    } else {
      return `${diffDays} days`;
    }
  };
}

exports.inject = function(module) {
  module.service('TimeService', TimeService);
};
