/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang

import org.apache.spark.sql.types.StructType

import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.inference.{InferContext, InferenceWarnings}

import ai.deepsense.deeplang.DPortPosition.DPortPosition

trait DataFrame2To1Operation { self: DOperation2To1[DataFrame, DataFrame, DataFrame] =>

  override def inPortsLayout: Vector[DPortPosition] =
    Vector(DPortPosition.Left, DPortPosition.Right)

  override protected final def inferKnowledge(
      leftDataFrameKnowledge: DKnowledge[DataFrame],
      rightDataFrameKnowledge: DKnowledge[DataFrame])(
      context: InferContext): (DKnowledge[DataFrame], InferenceWarnings) = {

    val leftSchema = leftDataFrameKnowledge.single.schema
    val rightSchema = rightDataFrameKnowledge.single.schema

    if (leftSchema.isDefined && rightSchema.isDefined) {
      val (outputSchema, warnings) = inferSchema(leftSchema.get, rightSchema.get)
      (DKnowledge(DataFrame.forInference(outputSchema)), warnings)
    } else {
      (DKnowledge(DataFrame.forInference()), InferenceWarnings.empty)
    }
  }

  protected def inferSchema(
      leftSchema: StructType,
      rightSchema: StructType): (StructType, InferenceWarnings) = {
    (StructType(Seq.empty), InferenceWarnings.empty)
  }
}
