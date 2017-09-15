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

export const specialOperations = {
  ACTIONS: {
    EVALUATE: 'a88eaf35-9061-4714-b042-ddd2049ce917',
    TRANSFORM: '643d8706-24db-4674-b5b4-10b5129251fc',
    FIT: '0c2ff818-977b-11e5-8994-feff819cdc9f',
    FIT_AND_TRANSFORM: '1cb153f1-3731-4046-a29b-5ad64fde093f'
  },
  NOTEBOOKS: {
    PYTHON: 'e76ca616-0322-47a5-b390-70c9668265dd',
    R: '89198bfd-6c86-40de-8238-68f7e0a0b50e'
  },
  CUSTOM_TRANSFORMER: {
    NODE: '65240399-2987-41bd-ba7e-2944d60a3404',
    SOURCE: 'f94b04d7-ec34-42f7-8100-93fe235c89f8',
    SINK: 'e652238f-7415-4da6-95c6-ee33808561b2'
  },
  DATASOURCE: {
    READ: '1a3b32f0-f56d-4c44-a396-29d2dfd43423',
    WRITE: 'bf082da2-a0d9-4335-a62f-9804217a1436'
  },
  UNKNOWN_OPERATION: '08752b37-3f90-4b8d-8555-e911e2de5662'
};
