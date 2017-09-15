/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Created by Oleksandr Tserkovnyi on 23.06.15.
 */
'use strict';

function Freeze() {
  return {
    restrict: 'A',
    link: function (scope, element, attrs) {
      var internal = {};
      const TO_FREEZE = true;

      internal.ANIMATION_OUT_CLASS  = attrs.freezeAnimationOut || 'fadeOutUp';
      internal.ANIMATION_IN_CLASS   = attrs.freezeAnimationIn || 'fadeInDown';
      internal.ANIMATION_OUT_CLASS_CONCAT = internal.ANIMATION_OUT_CLASS  + ' animated';
      internal.ANIMATION_IN_CLASS_CONCAT  = internal.ANIMATION_IN_CLASS   + ' animated';
      internal.STATIC_CLASS_STRING = 'freeze--static fa fa-lock';

      internal.toggleAnimationClass = function toggleAnimationClass (animationClass, classConcat, animate = 'OUT') {
        if (animate === 'IN') {
          element.removeClass('hidden');
          element.removeClass(internal.ANIMATION_OUT_CLASS_CONCAT);
        }

        element.removeClass(classConcat).addClass(classConcat);

        element.one('webkitAnimationEnd mozAnimationEnd MSAnimationEnd oanimationend animationend', function (e) {
          if (e.animationName !== animationClass) {
            return false;
          }

          element.removeClass(classConcat);

          if (animate === 'OUT') {
            element.addClass('hidden');
          }
        });
      };

      internal.staticStateToggle = function staticStateToggle (toFreeze) {
        if (getComputedStyle(element[0]).position === 'static') {
          internal.STATIC_CLASS_STRING += ' freeze--position';
        }

        if (toFreeze) {
          element.addClass(internal.STATIC_CLASS_STRING);
        } else {
          internal.STATIC_CLASS_STRING += ' freeze--position';
          element.removeClass(internal.STATIC_CLASS_STRING);
        }
      };

      internal.checkState = function checkState (state) {
        if (state === attrs.freezeOn) {
          internal.freezeThis();
        } else if (internal.frozen && state !== attrs.freezeOn) {
          internal.unFreezeThis();
        }
      };

      internal.freezeThis = function freezeThis () {
        if (attrs.freezeAnimationOut || attrs.freezeAnimationIn) {
          internal.toggleAnimationClass(internal.ANIMATION_OUT_CLASS, internal.ANIMATION_OUT_CLASS_CONCAT);
        } else {
          internal.staticStateToggle(TO_FREEZE);
        }

        internal.frozen = true;
      };

      internal.unFreezeThis = function unFreezeThis () {
        if (attrs.freezeAnimationOut || attrs.freezeAnimationIn) {
          internal.toggleAnimationClass(internal.ANIMATION_IN_CLASS, internal.ANIMATION_IN_CLASS_CONCAT, 'IN');
        } else {
          internal.staticStateToggle(!TO_FREEZE);
        }

        internal.frozen = false;
      };

      attrs.$observe('freeze', internal.checkState);
    }
  };
}

exports.inject = function (module) {
  module.directive('freeze', Freeze);
};
