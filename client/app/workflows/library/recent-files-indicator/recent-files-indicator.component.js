
import tpl from './recent-files-indicator.template.html';
import RecentFilesController from './recent-files-indicator.controller.js';

const RecentFilesIndicatorComponent = {
  controller: RecentFilesController,
  bindings: {
    onSelect: '<'
  },
  templateUrl: tpl
};

export default RecentFilesIndicatorComponent;
