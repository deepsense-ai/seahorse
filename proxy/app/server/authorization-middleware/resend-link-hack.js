/**
 * Copyright (c) 2017, CodiLime Inc.
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
