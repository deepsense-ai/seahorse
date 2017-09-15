/**
 * Copyright 2017, deepsense.ai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

const config = require('../config/config');
const moment = require('moment');
const _ = require('underscore');

function trialTimeLimitedMiddleware(req, res, next) {
  const creationTimeString = config.getMandatory('SEAHORSE_CREATION_DATE');
  const createDateEnvFormat = 'YYYY-MM-DD';
  const seahorseCreationTime = moment(creationTimeString, createDateEnvFormat);

  const trialPeriod = moment.duration(8, 'weeks');

  const currentTime = moment();
  const expirationDateTime = seahorseCreationTime.add(trialPeriod);

  const outputTimeFormat = 'YYYY-MM-DDTHH:mm:ss';

  const isTrialExpired = expirationDateTime.isBefore(currentTime);

  if(_.isUndefined(req.user)) {
    throw 'User must be defined in time-limit middleware';
  }

  const isCurrentUserAdmin = req.user.user_id == '172dce21-ea97-42cc-b2ff-30ce4ec7de9d';

  res.set('current-date-time', currentTime.format(outputTimeFormat));
  res.set('trial-expiration-date-time', expirationDateTime.format(outputTimeFormat));
  res.set('is-current-user-admin', isCurrentUserAdmin);
  console.log("Expiration date is ", expirationDateTime.format(outputTimeFormat))
  if (isTrialExpired) {
    if (!isCurrentUserAdmin) {
      res.redirect('/trial-expired.html');
      res.end();
      return
    }
  }

  return next()
}

module.exports = {
  middleware: trialTimeLimitedMiddleware,
};
