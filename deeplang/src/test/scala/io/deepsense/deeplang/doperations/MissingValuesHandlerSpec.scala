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

package io.deepsense.deeplang.doperations

import org.apache.spark.sql.types._

import io.deepsense.deeplang.parameters.{IndexRangeColumnSelection, MultipleColumnSelection}
import io.deepsense.deeplang.{DeeplangTestSupport, DKnowledge, UnitSpec}
import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.doperables.dataframe.{DataFrame, DataFrameBuilder, DataFrameMetadata}
import io.deepsense.deeplang.inference.InferContext

class MissingValuesHandlerSpec extends UnitSpec with DeeplangTestSupport {

  "Missing Values Handler" should {
    "infer proper metadata information" in {
      val schema = createMultiColumnSchema
      val inputMetadata = DataFrameMetadata.fromSchema(schema)
      val df = DataFrameBuilder.buildDataFrameForInference(inputMetadata)
      val inferContext = createInferContext(mock[DOperableCatalog], fullInference = true)

      val columnSelection = MultipleColumnSelection(
        Vector(IndexRangeColumnSelection(Some(0), Some(2))))

      val (knowledge, warnings) =
        new MissingValuesHandler()
          .setSelectedColumns(columnSelection)
          .setStrategy(MissingValuesHandler.Strategy.RemoveRow())
        .inferKnowledge(inferContext)(Vector(new DKnowledge[DataFrame](df)))

      warnings shouldBe empty
      knowledge should have size 1
      knowledge(0).types should have size 1

      val metadata = knowledge(0).types.head.inferredMetadata.get.asInstanceOf[DataFrameMetadata]
      metadata.toSchema shouldBe schema
    }
  }

  private def createMultiColumnSchema: StructType = {
    StructType(List(
      StructField("column1", IntegerType),
      StructField("column2", DoubleType),
      StructField("column3", StringType)
    ))
  }
}
