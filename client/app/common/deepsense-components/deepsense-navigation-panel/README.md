# Nagivation panel

The module provides with re-usable directives that are responsible for zooming and moving DOM elements.

*NOTE!* Some assumptions have been made. There can be distinguished two objects: `interactive element` and `interactive panel`.
The former object corresponds to the object which will be zoomed and moved, whereas the latter is an object in which
the interactive element is placed. The interactive panel is a **direct parent** of the interactive element.
Moreover, when it comes to zooming and moving, the desired effect is achieved when dimensions of the interactive element
**are not greater** than dimensions of the interactive panel. Please, make sure the last assumption will be satisfied after
attaching the directives!

### Dependencies

- angular
- bootstrap
- font-awesome
- jquery
- ng-debounce

### API

There are two provided directives:

* deepsense-interactive
* deepsense-interaction-toolbar

The first one gives opportunity for a user to do interactions with the element that the directive is attached to.
Possible interactions include moving and zooming in / out the element. The second one is a toolbar with some action
buttons.


### API -- deepsense-interactive

COMPONENT MODULE `deepsense.navigation-panel`

COMPONENT DIRECTIVE `deepsense-interactive`

The element, to which this directive is attached, is the interactive element (mentioned earlier, see the first paragraph).

EXAMPLE OF USAGE

    <ELEMENT ... deepsense-interactive
      zoom-id="{string}"
      max-zoom-ratio="{number}"
    ...>
    </ELEMENT>

ARGUMENTS

- `zoomId` | {string} | an identifier used for binding events across the directives
- `maxZoomRatio` | {number} | default value: `2.0` | maximum possible value of the zoom ratio

EVENTS

- `INTERACTION-PANEL.ZOOM-IN` | `$on()` | the directive captures this event which was triggered by a button in the toolbar.
  After capturing it, the element to which this directive has been attached, is zoomed in with respect to the
  centre of the element.
- `INTERACTION-PANEL.ZOOM-OUT` | `$on()` | analogously as above, but the element is zoomed out.
- `INTERACTION-PANEL.MOVE-GRAB` | `$on()` | if captured, a user is able to move the element accordingly to the mouse movement.
- `ZOOM.ZOOM_PERFORMED` | `$emit()` | this event is triggered each time when the element changes its zoom ratio.

### API -- deepsense-interaction-toolbar

COMPONENT MODULE `deepsense.navigation-panel`

COMPONENT DIRECTIVE `<deepsense-interaction-toolbar>`

EXAMPLE OF USAGE

    <deepsense-interaction-toolbar
      zoom-id="{String}">
    </deepsense-interaction-toolbar>

ARGUMENTS

- `zoomId` | {String} | an identifier used for binding the directives

EVENTS

- `INTERACTION-PANEL.ZOOM-IN` | `$rootScope.$broadcast()` | this event is broadcasted when a user clicks at the zoom-in button.
- `INTERACTION-PANEL.ZOOM-OUT` | `$rootScope.$broadcast()` | this event is broadcasted when a user clicks at the zoom-out button
- `INTERACTION-PANEL.MOVE-GRAB` | `$rootScope.$broadcast()` | this event is broadcasted when a user clicks at the move-grab button.

