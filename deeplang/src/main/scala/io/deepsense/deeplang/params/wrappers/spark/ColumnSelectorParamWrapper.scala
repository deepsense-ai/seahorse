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

import org.apache.spark.ml

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.parameters._
import io.deepsense.deeplang.params.ColumnSelectorParam
import io.deepsense.deeplang.params.wrappers.spark.SparkParamUtils.{defaultDescription, defaultName}


class ColumnSelectorParamWrapper(
    val sparkParam: ml.param.StringArrayParam,
    override val portIndex: Int = 0,
    override val index: Int = 0,
    val customName: Option[String] = None,
    val customDescription: Option[String] = None)
  extends ColumnSelectorParam(
    customName.getOrElse(defaultName(sparkParam)),
    customDescription.getOrElse(defaultDescription(sparkParam)),
    portIndex,
    index)
  with SparkParamWrapper[Array[String], MultipleColumnSelection] {

  override def convert(value: MultipleColumnSelection)(df: DataFrame): Array[String] =
    df.getColumnNames(value).toArray
}
