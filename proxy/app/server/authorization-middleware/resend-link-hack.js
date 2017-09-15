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

const _ = require('lodash');

module.exports = {
    forwardResendActivationLinkToForgotPasswordLink: allowResendActivationLink,
};

/**
 * This is a hack for Cloudfoundry authorization pointing 'Resend activation link'
 * to 'create_account' page. Access to 'create_account' is restricted depending on
 * quota settings. This is a hack that redirects from 'create_account' page to 
 * 'forgot_password' when 'Resend activation link' was clicked. 
 */
function allowResendActivationLink(req, res, next) {
    const resendActivationLinkSource = '/authorization/accounts/email_sent'
    if (_.endsWith(req.headers.referer, resendActivationLinkSource)) {
        res.redirect('/authorization/forgot_password')
    } else {
        next()
    }
}
