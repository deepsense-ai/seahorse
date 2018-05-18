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

package ai.deepsense.deeplang.doperations

import scala.reflect.runtime.{universe => ru}

import org.apache.spark.sql.types.StructType

import ai.deepsense.commons.utils.Version
import ai.deepsense.deeplang.DOperation.Id
import ai.deepsense.deeplang.DPortPosition.DPortPosition
import ai.deepsense.deeplang.documentation.OperationDocumentation
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperations.exceptions.SchemaMismatchException
import ai.deepsense.deeplang.inference.InferenceWarnings
import ai.deepsense.deeplang.params.Params
import ai.deepsense.deeplang.{DOperation2To1, DPortPosition, DataFrame2To1Operation, ExecutionContext}
import ai.deepsense.sparkutils.SQL

case class Union()
    extends DOperation2To1[DataFrame, DataFrame, DataFrame]
    with DataFrame2To1Operation
    with Params
    with OperationDocumentation {

  override val id: Id = "90fed07b-d0a9-49fd-ae23-dd7000a1d8ad"
  override val name: String = "Union"
  override val description: String =
    "Creates a DataFrame containing all rows from both input DataFrames"


  override def inPortsLayout: Vector[DPortPosition] =
    Vector(DPortPosition.Left, DPortPosition.Right)

  override val since: Version = Version(0, 4, 0)

  val specificParams: Array[ai.deepsense.deeplang.params.Param[_]] = Array()

  override protected def execute(
    first: DataFrame, second: DataFrame)(
    context: ExecutionContext): DataFrame = {

    inferSchema(first.schema.get, second.schema.get)

    context.dataFrameBuilder.buildDataFrame(
      first.schema.get,
      SQL.union(first.sparkDataFrame, second.sparkDataFrame).rdd)
  }

  protected override def inferSchema(
      leftSchema: StructType,
      rightSchema: StructType): (StructType, InferenceWarnings) = {
    if (leftSchema.treeString != rightSchema.treeString) {
      throw new SchemaMismatchException(
        "SchemaMismatch. Expected schema " +
          s"${leftSchema.treeString}" +
          s" differs from ${rightSchema.treeString}")
    }
    (leftSchema, InferenceWarnings.empty)
  }

  @transient
  override lazy val tTagTI_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
  @transient
  override lazy val tTagTI_1: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
  @transient
  override lazy val tTagTO_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
}
