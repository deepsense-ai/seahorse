/**
 * Copyright (c) 2015, CodiLime Inc.
 */
/*global browser*/
'use strict';

describe('Home page', function() {

    it('should have proper page title & content', function() {
        browser.get('/#/');

        expect(browser.getTitle()).toEqual('DeepSense.io LAB');
        expect($('.container-fluid p').getText()).toEqual('Hello!');
    });
});
