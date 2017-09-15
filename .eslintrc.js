module.exports = {
  env: {
    browser: true,
    es6: true,
    jasmine: true
  },

  extends: [
    // 'eslint:recommended',
    // 'angular'
  ],

  globals: {
    _: true,        // Should be inported when necessary
    $: true,        // Should be inported when necessary
    angular: true,  // Should be inported when necessary
    exports: true,
    jsPlumb: true,  // Should be inported when necessary
    jQuery: true,   // Should be inported when necessary
    module: true,
    moment: true,   // Should be inported when necessary
    require: true   // Refactor to use ES6 import
  },


  parserOptions: {
    ecmaVersion: 6,
    sourceType: 'module',
    ecmaFeatures: {}
  },

  plugins: [
    'angular'
  ],

  root: true,

  rules: {
    // {{{ ESLint

    // {{{ ESLint : Possible Errors
    'no-cond-assign': 'error',
    'no-console': 'error',
    'no-constant-condition': 'error',
    'no-control-regex': 'error',
    'no-debugger': 'error',
    'no-dupe-args': 'error',
    'no-dupe-keys': 'error',
    'no-duplicate-case': 'error',
    'no-empty-character-class': 'error',
    'no-empty': 'error',
    'no-ex-assign': 'error',
    'no-extra-boolean-cast': 'error',
    'no-extra-parens': 'off',
    'no-extra-semi': 'error',
    'no-func-assign': 'error',
    'no-inner-declarations': 'error',
    'no-invalid-regexp': 'error',
    'no-irregular-whitespace': 'error',
    'no-obj-calls': 'error',
    'no-prototype-builtins': 'off',
    'no-regex-spaces': 'error',
    'no-sparse-arrays': 'error',
    'no-template-curly-in-string': 'warn',
    'no-unexpected-multiline': 'error',
    'no-unreachable': 'error',
    'no-unsafe-finally': 'error',
    'no-unsafe-negation': 'error',
    'use-isnan': 'error',
    'valid-jsdoc': 'off',
    'valid-typeof': 'error',
    // ESLint : Possible Errors }}}

    // {{{ ESLint : Best Practices
    'accessor-pairs': 'error',
    'array-callback-return': 'error',
    'block-scoped-var': 'error',
    'class-methods-use-this': 'off',
    // 'complexity': ['warn', 6],
    'consistent-return': 'off',
    'curly': 'error',
    'default-case': 'error',
    'dot-location': ['error', 'property'],
    'dot-notation': 'error',
    'eqeqeq': 'error',
    // 'guard-for-in': 'error',
    // 'no-alert': 'warn',
    'no-caller': 'error',
    'no-case-declarations': 'error',
    'no-div-regex': 'error',
    // 'no-else-return': 'error',
    'no-empty-function': 'off',
    'no-empty-pattern': 'error',
    'no-eq-null': 'error',
    'no-eval': 'error',
    'no-extend-native': 'error',
    'no-extra-bind': 'error',
    'no-extra-label': 'error',
    'no-fallthrough': 'error',
    'no-floating-decimal': 'error',
    'no-global-assign': 'error',
    'no-implicit-coercion': 'off',
    'no-implicit-globals': 'error',
    'no-implied-eval': 'error',
    'no-invalid-this': 'off',
    'no-iterator': 'off',
    'no-labels': 'off',
    'no-lone-blocks': 'error',
    'no-loop-func': 'error',
    'no-magic-numbers': 'off',
    'no-multi-spaces': 'error',
    'no-multi-str': 'error',
    'no-new-func': 'error',
    'no-new-wrappers': 'error',
    'no-new': 'error',
    'no-octal-escape': 'error',
    'no-octal': 'error',
    // 'no-param-reassign': 'error',
    'no-proto': 'error',
    'no-redeclare': 'error',
    'no-restricted-properties': 'off',
    'no-return-assign': 'error',
    'no-script-url': 'error',
    'no-self-assign': 'error',
    'no-self-compare': 'error',
    'no-sequences': 'error',
    'no-throw-literal': 'off',
    'no-unmodified-loop-condition': 'error',
    'no-unused-expressions': 'error',
    'no-unused-labels': 'error',
    'no-useless-call': 'error',
    'no-useless-concat': 'error',
    'no-useless-escape': 'error',
    'no-void': 'off',
    // 'no-warning-comments': 'warn',
    'no-with': 'error',
    'radix': 'error',
    // 'vars-on-top': 'error',
    'wrap-iife': ['error', 'any'],
    'yoda': 'error',
    // ESLint : Best Practices }}}


    // {{{ ESLint : Strict Mode
    // 'strict': 'warn',
    // ESLint : Strict Mode }}}

    // {{{ ESLint : Variables
    'init-declarations': 'off',
    'no-catch-shadow': 'off',
    'no-delete-var': 'error',
    'no-label-var': 'off',
    'no-restricted-globals': 'off',
    'no-shadow-restricted-names': 'off',
    'no-shadow': 'off',
    'no-undef-init': 'error',
    'no-undef': 'error',
    'no-undefined': 'off',
    // 'no-unused-vars': 'error',
    'no-use-before-define': 'off',
    // ESLint : Variables }}}

    // {{{ ESLint : Stylistic Issues
    'array-bracket-spacing': ['error', 'never'],
    'block-spacing': 'error',
    // 'brace-style': 'error',
    'camelcase': 'error',
    'comma-dangle': 'error',
    'comma-spacing': 'error',
    'comma-style': 'error',
    'computed-property-spacing': 'error',
    'consistent-this': 'off',
    'eol-last': 'error',
    'func-call-spacing': 'error',
    // 'func-names': 'error',
    // 'func-style': ['error', 'declaration', {
    //   allowArrowFunctions: true
    // }],  // 20 errors
    'id-blacklist': 'off',
    'id-length': 'off',
    'id-match': 'off',
      // TODO: to be defined
      // 'indent': ['error', 2, {
      //   'SwitchCase': 1
      // }],
      // enforce consistent indentation
    'jsx-quotes': ['error', 'prefer-double'],
    'key-spacing': ['error', {
      singleLine: {
        beforeColon: false,
        afterColon: true
      },
      multiLine: {
        beforeColon: false,
        afterColon: true
      }
    }],
    // TODO: to be defined
    // 'keyword-spacing'
    // enforce consistent spacing before and after keywords
    'line-comment-position': 'off',
    'linebreak-style': ['error', 'unix'],
    'lines-around-comment': 'off',
    'lines-around-directive': ['error', 'always'],
    'max-depth': ['warn', 4],
    // 'max-len': ['error', {   // 123 errors
    //   code: 100,
    //   tabWidth: 4,
    //   comments: 120,
    //   ignoreUrls: true,
    //   ignoreStrings: true,
    //   ignoreTemplateLiterals: true
    // }],
    'max-lines': 'off',
    'max-nested-callbacks': ['warn', 5],
    'max-params': 'off',
    'max-statements-per-line': ['warn', {max: 2}],
    'max-statements': 'off',
    'multiline-ternary': 'off',
    // 'new-cap': 'warn',
    'new-parens': 'warn',
    'newline-after-var': 'off',
    // 'newline-before-return': 'error',
    // 'newline-per-chained-call': 'warn',
    'no-array-constructor': 'error',
    'no-bitwise': 'warn',
    'no-continue': 'off',
    'no-inline-comments': 'off',
    'no-lonely-if': 'error',
    'no-mixed-operators': 'off',
    'no-mixed-spaces-and-tabs': 'error',
    'no-multiple-empty-lines': ['error', {
      max: 2,
      maxEOF: 1,
      maxEOF: 1
    }],
    'no-negated-condition': 'off',
    // 'no-nested-ternary': 'warn',
    'no-new-object': 'error',
    'no-plusplus': 'off',
    'no-restricted-syntax': ['error', 'WithStatement'],
    'no-tabs': 'error',
    'no-ternary': 'off',
    'no-trailing-spaces': 'error',
    'no-underscore-dangle': 'off',
    'no-unneeded-ternary': 'off',
    'no-whitespace-before-property': 'error',
    'object-curly-newline': 'off',
    'object-curly-spacing': 'off',
    'object-property-newline': 'off',
    'one-var-declaration-per-line': 'error',
    'one-var': ['error', 'never'],
    'operator-assignment': 'off',
    'operator-linebreak': ['error', 'after'],
    'padded-blocks': 'off',
    'quote-props': 'off',
    'quotes': ['error', 'single'],
    'require-jsdoc': 'off',
    'semi-spacing': 'error',
    'semi': 'error',
    'sort-keys': 'off',
    'sort-vars': 'off',
    'space-before-blocks': ['error', 'always'],
    'space-before-function-paren': ['error', {
      anonymous: 'ignore',
      named: 'never'
    }],
    'space-in-parens': ['error', 'never'],
    'space-infix-ops': 'error',
    'space-unary-ops': ['error', {
      words: true,
      nonwords: false
    }],
    'spaced-comment': ['error', 'always', {
      block: {
        balanced: true
      }
    }],
    'unicode-bom': ['error', 'never'],
    'wrap-regex': 'off',
    // ESLint : Stylistic Issues }}}

    // {{{ ESLint : ECMAScript 6
    'arrow-body-style': 'off',
    'arrow-parens': 'off',
    'arrow-spacing': ['error', {
        "before": true,
        "after": true
    }],
    'constructor-super': 'error',
    'generator-star-spacing': ['error', {before: true, after: false}],
    'no-class-assign': 'error',
    'no-confusing-arrow': ['error', {allowParens: true}],
    'no-const-assign': 'error',
    'no-dupe-class-members': 'error',
    'no-duplicate-imports': ['error', {includeExports: true}],
    'no-new-symbol': 'error',
    'no-restricted-imports': 'off',
    'no-this-before-super': 'error',
    'no-useless-computed-key': 'error',
    // 'no-useless-constructor': 'error',
    'no-useless-rename': 'error',
    // 'no-var': 'warn',
    'object-shorthand': 'off',
    // 'prefer-arrow-callback': 'off',
    // 'prefer-const': ['warn', {
    //     destructuring: 'all',
    //     ignoreReadBeforeAssign: true
    // }],
    'prefer-numeric-literals': 'error',
    'prefer-reflect': 'off',
    // 'prefer-rest-params': 'error',
    // 'prefer-spread': 'error',
    // 'prefer-template': 'error',
    'require-yield': 'error',
    'rest-spread-spacing': ['error', 'never'],
    'sort-imports': 'off',
    'symbol-description': 'error',
    // 'template-curly-spacing': ['error', 'never'],
    'yield-star-spacing': ['error', 'before'],
    // ESLint : ECMAScript 6 }}}

    // ESLint }}}


    // {{{ Angular

    // {{{ Angular : Possible Errors
    // 'angular/module-getter': 'error', // disallow to reference modules with variables and require to use the getter syntax instead angular.module('myModule') (y022)
    // 'angular/module-setter': 'error', // disallow to assign modules to variables (linked to module-getter (y021)
    // 'angular/no-private-call': 'error', // disallow use of internal angular properties prefixed with $$
    // Angular : Possible Errors }}}

    // {{{ Angular : Best Practices
    // 'angular/component-limit': 'error', // limit the number of angular components per file (y001)
    // 'angular/controller-as-route' // require the use of controllerAs in routes or states (y031)
    // 'angular/controller-as-vm' // require and specify a capture variable for this in controllers (y032)
    // 'angular/controller-as' // disallow assignments to $scope in controllers (y031)
    // 'angular/deferred': 'error', // use $q(function(resolve, reject){}) instead of $q.deferred
    // 'angular/di-unused': 'error', // disallow unused DI parameters
    'angular/directive-restrict': 'error', // disallow any other directive restrict than 'A' or 'E' (y074)
    'angular/empty-controller': 'error', // disallow empty controllers
    // 'angular/no-controller' // disallow use of controllers (according to the component first pattern)
    'angular/no-inline-template': 'error', // disallow the use of inline templates
    'angular/no-run-logic': 'error', // keep run functions clean and simple (y171)
    'angular/no-services': 'error', // disallow DI of specified services for other angular components ($http for controllers, filters and directives)
    // 'angular/on-watch': 'error', // require $on and $watch deregistration callbacks to be saved in a variable
    // 'angular/prefer-component' //
    // Angular : Best Practices }}}

    // {{{ Angular : Deprecated Angular Features
    'angular/no-cookiestore': 'error', // use $cookies instead of $cookieStore
    // 'angular/no-directive-replace': 'warn', // disallow the deprecated directive replace property
    'angular/no-http-callback': 'error', // disallow the $http methods success() and error()
    // Angular : Deprecated Angular Features }}}

    // {{{ Angular : Naming
    // 'angular/component-name': ['error', 'ds'], // require and specify a prefix for all component names
    // 'angular/controller-name': 'error', // require and specify a prefix for all controller names (y123, y124)
    // 'angular/directive-name': ['error', 'ds'], // require and specify a prefix for all directive names (y073, y126)
    // 'angular/file-name': ['error', {
    //   nameStyle: 'dash',
    //   typeSeparator: 'dot',
    //   ignoreTypeSuffix: true
    // }], // require and specify a consistent component name pattern (y120, y121)
    // 'angular/filter-name': ['error', 'ds'], // require and specify a prefix for all filter names
    'angular/module-name': 'off', // require and specify a prefix for all module names (y127)
    'angular/service-name': 'off', // require and specify a prefix for all service names (y125)
    // Angular : Naming }}}

    // {{{ Angular : Conventions
    'angular/di-order': 'off', // require DI parameters to be sorted alphabetically
    // 'angular/di': [2, '$inject'], // require a consistent DI syntax
    'angular/dumb-inject': 'error', // unittest inject functions should only consist of assignments from injected values to describe block variables
    // 'angular/function-type': ['error', 'named'], // require and specify a consistent function style for components ('named' or 'anonymous') (y024)
    // 'angular/module-dependency-order': ['error', {
    //   grouped: true,
    //   prefix: 'app'
    // }], // require a consistent order of module dependencies
    'angular/no-service-method': 'off', // use factory() instead of service() (y040)
    'angular/one-dependency-per-line': 'off', // require all DI parameters to be located in their own line
    'angular/rest-service': ['error', '$http'], // disallow different rest service and specify one of '$http', '$resource', 'Restangular'
    'angular/watchers-execution': 'off', // require and specify consistent use $scope.digest() or $scope.apply()
    // Angular : Conventions}}}

    // {{{ Angular : Angular Wrappers
    // 'angular/angularelement': 'error', // use angular.element instead of $ or jQuery
    'angular/definedundefined': 'off', // use angular.isDefined and angular.isUndefined instead of other undefined checks
    // 'angular/document-service': 'error', // use $document instead of document (y180)
    'angular/foreach': 'off', // use angular.forEach instead of native Array.prototype.forEach
    'angular/interval-service': 'error', // use $interval instead of setInterval (y181)
    'angular/json-functions': 'off', // use angular.fromJson and 'angular.toJson' instead of JSON.parse and JSON.stringify
    // 'angular/log': 'error', // use the $log service instead of the console methods
    'angular/no-angular-mock': 'off', // require to use angular.mock methods directly
    'angular/no-jquery-angularelement': 'error', // disallow to wrap angular.element objects with jQuery or $
    'angular/timeout-service': 'error', // use $timeout instead of setTimeout (y181)
    'angular/typecheck-array': 'off', // use angular.isArray instead of typeof comparisons
    'angular/typecheck-date': 'off', // use angular.isDate instead of typeof comparisons
    'angular/typecheck-function': 'off', // use angular.isFunction instead of typeof comparisons
    'angular/typecheck-number': 'off', // use angular.isNumber instead of typeof comparisons
    'angular/typecheck-object': 'off', // use angular.isObject instead of typeof comparisons
    'angular/typecheck-string': 'off', // use angular.isString instead of typeof comparisons
    // 'angular/window-service': 'error' // use $window instead of window (y180)
    // Angular : Angular Wrappers }}}

    // Angular }}}
  }
};
