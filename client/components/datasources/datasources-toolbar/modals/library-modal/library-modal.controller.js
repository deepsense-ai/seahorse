'use strict';

import footerTpl from '../modal-footer/modal-footer.html';

class LibraryModalController {
  constructor($uibModalInstance) {
    'ngInject';

    this.footerTpl = footerTpl;

    _.assign(this, {$uibModalInstance});
  }

  cancel() {
    this.$uibModalInstance.dismiss();
  }

  ok() {
    this.$uibModalInstance.close();
  }
}

export default LibraryModalController;
