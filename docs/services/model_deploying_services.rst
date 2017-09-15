.. Copyright (c) 2015, CodiLime, Inc.

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
