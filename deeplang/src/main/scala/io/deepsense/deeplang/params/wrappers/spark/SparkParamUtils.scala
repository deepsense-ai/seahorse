/**
 * Copyright 2015, deepsense.io
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

package io.deepsense.deeplang.params.wrappers.spark

import org.apache.spark.ml.param.Param

object SparkParamUtils {

  def defaultName(sparkParam: Param[_]): String =
    sparkParam.name.replaceAll("([A-Z])", " $1").toLowerCase

  def defaultDescription(sparkParam: Param[_]): String =
    sparkParam.doc.substring(0, 1).toUpperCase + sparkParam.doc.substring(1)

}
