class ColorPicker {
  constructor () {
    this.restrict = 'A';
    this.templateUrl = 'color-picker/color-picker.html';
    this.replace = true;
    this.controller = 'ColorPickerController';
    this.controllerAs = 'colorPicker';
  }

  static directiveFactory () {
    ColorPicker.instance = new ColorPicker();
    return ColorPicker.instance;
  }
}

class ColorPickerController {
  /* @ngInject */
  constructor ($scope, $element, $compile, $document) {
    this.$scope = $scope;
    this.$element = $element;
    this.$compile = $compile;
    this.$document = $document;
    this.$body = $(this.$document[0].body);
    this.picker = null;
    this.template =
      `<section class="c-color-picker" data-close="false">
        <header class="c-color-picker__head">
          <label for="colorPicker" class="block">
            Select new color
            <span class="c-color-picker__save-indicator pull-right animated fadeInRight"
                  ng-show="node.color !== predefColors[predefColors.length - 1]">saved</span>
          </label>
          <input  id="colorPicker"
                  type="color"
                  ng-model="node.color"
                  class="c-color-picker__input"
                  autofocus
                  focused />
        </header>
        <article class="c-color-picker__content">
          <p>Select from used colors</p>
          <ul class="c-predefined-colors list-unstyled">
            <li class="c-predefined-colors__color"
                ng-style="{'background': predefColor}"
                ng-click="node.color = predefColor"
                ng-repeat="predefColor in predefColors"
                tooltip="{{::$first ? 'This is default color' : ''}}">
              <a class="c-predefined-colors__remove-color"
                 aria-label="Remove color"
                 ng-click="colorPicker.removeColor($index)"
                 ng-if="$index > 5"
                 tooltip="Remove this color"
                 tooltip-append-to-body="true"
                 data-close="false">
                <i class="fa fa-times"></i>
              </a>
            </li>
          </ul>
        </article>
        <footer class="text-right">
          <button class="btn btn-info btn-sm" ng-click="colorPicker.close()">Close</button>
        </footer>
      </section>`;

    // close on click on body
    $document.on('click', this.checkAndClose.bind(this));
  }

  open (event) {
    if (!this.picker) {
      this.picker = this.$compile(this.template)(this.$scope);

      this.$body.append(this.picker);

      $(this.picker).css({
        'top': $(event.target).offset().top,
        'left': $(event.target).offset().left - $(this.picker).outerWidth(true)
      });

      this.opened = true;
    }
  }

  close () {
    if (this.picker) {
      this.saveChosenColor();
      this.picker.remove();
      this.picker = null;
    }
  }

  checkAndClose (event) {
    if (
      this.opened &&
      !event.target.closest('[data-close="false"]')
    ) {
      this.close();
      this.opened = false;
    }
  }

  saveChosenColor () {
    if (this.$scope.predefColors.indexOf(this.$scope.node.color) === -1) {
      this.$scope.predefColors.push(this.$scope.node.color);
    }
  }

  removeColor (index) {
    this.$scope.predefColors.splice(index, 1);
  }
}

angular.module('deepsense.attributes-panel')
  .directive('colorPicker', ColorPicker.directiveFactory)
  .controller('ColorPickerController', ColorPickerController);

