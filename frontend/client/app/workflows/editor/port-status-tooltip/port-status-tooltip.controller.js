/**
 * Copyright 2017 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
