/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.params.wrappers.spark

import org.apache.spark.ml
import org.apache.spark.sql.types.StructType

import ai.deepsense.deeplang.doperables.dataframe.DataFrameColumnsGetter
import ai.deepsense.deeplang.params.SingleColumnSelectorParam
import ai.deepsense.deeplang.params.selections.SingleColumnSelection

class SingleColumnSelectorParamWrapper[P <: ml.param.Params](
    override val name: String,
    override val description: Option[String],
    val sparkParamGetter: P => ml.param.Param[String],
    override val portIndex: Int)
  extends SingleColumnSelectorParam(name, description, portIndex)
  with SparkParamWrapper[P, String, SingleColumnSelection] {

  override def convert(value: SingleColumnSelection)(schema: StructType): String =
    DataFrameColumnsGetter.getColumnName(schema, value)

  override def replicate(name: String): SingleColumnSelectorParamWrapper[P] =
    new SingleColumnSelectorParamWrapper[P](name, description, sparkParamGetter, portIndex)
}
