/**
 * demo.browserify.mocks are used only for demo to prevent errors from CommonJS functions not being available natively
 * inside the browser.
 *
 * deepsense-components require js files using CommonJS API (supported by browserify).
 * CommonJS modules API are not available in browser, so without building them, code wouldn't be executed properly.
 * In the demo we stub CommonJS API and we load explicitly all files in index.html
 */
window.require = function() {};
window.module = {
  exports: function() {}
};
