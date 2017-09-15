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

package ai.deepsense.deeplang.doperables

import org.apache.spark.sql.types._

import ai.deepsense.deeplang._
import ai.deepsense.deeplang.doperables.dataframe._
import ai.deepsense.deeplang.params.Param
import ai.deepsense.deeplang.params.choice.ChoiceParam

case class TypeConverter() extends MultiColumnTransformer {

  val targetType = ChoiceParam[TargetTypeChoice](
    name = "target type",
    description = Some("Target type of the columns."))

  def getTargetType: TargetTypeChoice = $(targetType)
  def setTargetType(value: TargetTypeChoice): this.type = set(targetType, value)

  override def getSpecificParams: Array[Param[_]] = Array(targetType)

  override def transformSingleColumn(
      inputColumn: String,
      outputColumn: String,
      context: ExecutionContext,
      dataFrame: DataFrame): DataFrame = {
    val targetTypeName = getTargetType.columnType.typeName
    val expr = s"cast(`$inputColumn` as $targetTypeName) as `$outputColumn`"
    val sparkDataFrame = dataFrame.sparkDataFrame.selectExpr("*", expr)
    DataFrame.fromSparkDataFrame(sparkDataFrame)
  }

  override def transformSingleColumnSchema(
      inputColumn: String,
      outputColumn: String,
      schema: StructType): Option[StructType] = {
    MultiColumnTransformer.assertColumnExist(inputColumn, schema)
    MultiColumnTransformer.assertColumnDoesNotExist(outputColumn, schema)
    Some(schema.add(StructField(outputColumn, getTargetType.columnType, nullable = true)))
  }
}
