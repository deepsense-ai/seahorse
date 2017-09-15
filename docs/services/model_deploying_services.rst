..
  Copyright 2015, CodiLime Inc.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.

Model deploying service
=======================

===========
Description
===========

Stores trained RidgeRegression, LogisticRegression models and responds to score requests.

**Service version:** 1

**Since:** DeepSense 0.1.0

==========
REST API
==========

**POST /regression**

  content:

    .. code-block:: json

       {
         "isLogistic" : true,
         "intercept" : 2.4,
         "weights" : [1.2, 2.5, 3.7, 4.8],
         "means" : [1.2, 1.3, 3.7, 7.8],
         "stdDevs" : [1.1, 1.9, 5.7, 7.7]
       }

  result:
    .. code-block:: json

       {
         "id" : "3097218c-f720-4170-bf40-b5162f4734ab"
       }


**GET /regression/:id**

  content:
    .. code-block:: json

       {
         "features" : [1.1, 1.9, 5.7, 7.7]
       }

  result:
    .. code-block:: json

       {
         "score": 0.1
       }
