/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperations

import org.apache.spark.sql.types._

import io.deepsense.deeplang.catalogs.doperable.DOperableCatalog
import io.deepsense.deeplang.doperables.dataframe.{DataFrameBuilder, DataFrameMetadata, DataFrame}
import io.deepsense.deeplang.inference.InferContext
import io.deepsense.deeplang.{UnitSpec, DKnowledge}

class SplitSpec extends UnitSpec {

  "Split" should {
    "infer proper metadata information" in {
      val schema = createMultiColumnSchema
      val inputMetadata = DataFrameMetadata.fromSchema(schema)
      val df = DataFrameBuilder.buildDataFrameForInference(inputMetadata)
      val inferContext = InferContext(
        mock[DOperableCatalog],
        fullInference = true)

      val (knowledge, warnings) = Split(0.1, 1L)
        .inferKnowledge(inferContext)(Vector(new DKnowledge[DataFrame](df)))

      warnings shouldBe empty
      knowledge should have size 2
      knowledge(0).types should have size 1
      knowledge(1).types should have size 1

      val metadata1 = knowledge(0).types.head.inferredMetadata.get.asInstanceOf[DataFrameMetadata]
      metadata1.toSchema shouldBe schema
      val metadata2 = knowledge(1).types.head.inferredMetadata.get.asInstanceOf[DataFrameMetadata]
      metadata2.toSchema shouldBe schema
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
