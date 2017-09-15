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

package ai.deepsense.deeplang.doperables.spark.wrappers.transformers

import org.apache.spark.ml.feature.{VectorAssembler => SparkVectorAssembler}

import ai.deepsense.deeplang.doperables.SparkTransformerWrapper
import ai.deepsense.deeplang.params.Param
import ai.deepsense.deeplang.params.selections.{MultipleColumnSelection, NameColumnSelection}
import ai.deepsense.deeplang.params.wrappers.spark.{ColumnSelectorParamWrapper, SingleColumnCreatorParamWrapper}

class VectorAssembler extends SparkTransformerWrapper[SparkVectorAssembler] {

  val inputColumns = new ColumnSelectorParamWrapper[SparkVectorAssembler](
    name = "input columns",
    description = Some("The input columns."),
    sparkParamGetter = _.inputCols,
    portIndex = 0)

  val outputColumn = new SingleColumnCreatorParamWrapper[SparkVectorAssembler](
    name = "output column",
    description = Some("The name of created output column."),
    sparkParamGetter = _.outputCol)

  override val params: Array[Param[_]] = Array(inputColumns, outputColumn)

  def setInputColumns(selection: Set[String]): this.type = {
    set(inputColumns, MultipleColumnSelection(Vector(NameColumnSelection(selection))))
  }

  def setOutputColumn(name: String): this.type = {
    set(outputColumn, name)
  }
}
