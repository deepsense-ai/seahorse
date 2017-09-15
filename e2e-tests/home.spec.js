/**
 * Copyright (c) 2015, CodiLime Inc.
 */
/*global browser*/
'use strict';

describe('Home page', function() {
    it('should have proper page title & content', function() {
        browser.get('/#/');
        expect(browser.getTitle()).toEqual('DeepSense.io LAB');
        expect($('#page-wrapper .wrapper-content p').getText()).toEqual('Hello! DeepSense.io engine at Your service!');
    });
});
