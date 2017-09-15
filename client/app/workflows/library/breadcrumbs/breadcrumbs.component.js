
import tpl from './breadcrumbs.template.html';
import BreadcrumbsController from './breadcrumbs.controller.js';

const BreadcrumbsComponent = {
  controller: BreadcrumbsController,
  bindings: {
    allParents: '<',
    currentFolder: '<'
  },
  templateUrl: tpl
};

export default BreadcrumbsComponent;
