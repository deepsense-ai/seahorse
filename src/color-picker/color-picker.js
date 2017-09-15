class ColorPicker {
  constructor () {
    this.restrict = 'A';
    this.scope = {};
    this.templateUrl = 'color-picker/color-picker.html';
    this.replace = true;
    this.controller = 'ColorPickerController';
    this.controllerAs = 'colorPicker';
    this.bindToController = true;
  }

  static directiveFactory () {
    ColorPicker.instance = new ColorPicker();
    return ColorPicker.instance;
  }
}

class ColorPickerController {
  /* @ngInject */
  constructor ($scope, $compile, $document) {
    this.$scope = $scope;
    this.$compile = $compile;
    this.$document = $document;
    this.template =
      `<section class="c-color-picker">
        <label>
          <input type="color" ng-model="color" />
        </label>

        <ul>
          <li class="c-predefined-colors__color"
              ng-style="{'background': predefColor}"
              ng-click="color = predefColor"
              ng-repeat="predefColor in predefColors">
            {{predefColor}}
          </li>
        </ul>

        {{color}}
      </section>`;
    this.picker = null;
  }

  open () {
    this.picker = this.$compile(this.template)(this.$scope);
    console.log(this.$document, this.picker);
    this.$document.append(this.picker);
  }
}

angular.module('deepsense.attributes-panel')
  .directive('colorPicker', ColorPicker.directiveFactory)
  .controller('ColorPickerController', ColorPickerController);

