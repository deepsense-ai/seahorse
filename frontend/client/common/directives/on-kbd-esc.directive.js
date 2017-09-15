'use strict';


export default function onKbdEscDirective() {
  const ESC_KEY_CODE = 27;

  return function onKbdEsc(scope, element, attrs) {
    element.bind('keydown keypress', (event) => {
      if (event.which === ESC_KEY_CODE) {
        scope.$apply(() => {
          scope.$eval(attrs.onKbdEsc);
        });

        event.preventDefault();
      }
    });
  };
}
