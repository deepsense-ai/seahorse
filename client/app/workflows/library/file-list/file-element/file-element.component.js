import FileElementController from './file-element.controller.js';

const FileElementComponent = {
  controller: FileElementController,
  bindings: {
    item: '<',
    isLast: '<',
    onSelect: '<'
  },
  template: `
    <div ng-include="$ctrl.templateUrl"></div>
  `
};

export default FileElementComponent;
