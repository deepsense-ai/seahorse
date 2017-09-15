/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
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

module.exports = {
  env: {
    es6: true
  },

  parserOptions: {
    ecmaVersion: 6,
    sourceType: 'module',
    ecmaFeatures: {}
  },

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
    'brace-style': ['error', '1tbs', {
      'allowSingleLine': true
    }],
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
    'spaced-comment': 'off',
    'unicode-bom': ['error', 'never'],
    'wrap-regex': 'off',
    // ESLint : Stylistic Issues }}}

    // {{{ ESLint : ECMAScript 6
    'arrow-body-style': 'off',
    'arrow-parens': 'off',
    'arrow-spacing': ['error', {
        'before': true,
        'after': true
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
    'no-useless-constructor': 'error',
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
    'template-curly-spacing': ['error', 'never'],
    'yield-star-spacing': ['error', 'before']
    // ESLint : ECMAScript 6 }}}

    // ESLint }}}
  }
};
