'use strict';

class PortStatusTooltipController {
  constructor() {
    'ngInject';

    this.outputTypes = [];
  }

  getTypes() {
    this.outputTypes = this.portObject.typeQualifier.map((typeQualifier) => _.last(typeQualifier.split('.')));
    if (this.outputTypes.length > 3) {
      this.outputTypes = this.outputTypes.slice(0, 3);
      this.outputTypes.push('...');
    }
    return this.outputTypes;
  }
}

export default PortStatusTooltipController;
