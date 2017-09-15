'use strict';

import footerTpl from '../modal-footer/modal-footer.html';

class DatabaseModalController {
  constructor($uibModalInstance) {
    'ngInject';

    this.footerTpl = footerTpl;

    _.assign(this, {$uibModalInstance});
    this.formats = ['com.mysql.jdbc.driver', 'com.postgresql.jdbc.driver', 'com.oracle.jdbc.driver'];
  }

  cancel() {
    this.$uibModalInstance.dismiss();
  }

  ok() {
    this.$uibModalInstance.close();
  }

}

export default DatabaseModalController;
