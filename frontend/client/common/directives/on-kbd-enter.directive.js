'use strict';


export default function onKbdEnterDirective() {
  const ENTER_KEY_CODE = 13;

  return function onKbdEnter(scope, element, attrs) {
    element.bind('keydown keypress', (event) => {
      if (event.which === ENTER_KEY_CODE) {
        scope.$apply(() => {
          scope.$eval(attrs.onKbdEnter);
        });

        event.preventDefault();
      }
    });
  };
}
