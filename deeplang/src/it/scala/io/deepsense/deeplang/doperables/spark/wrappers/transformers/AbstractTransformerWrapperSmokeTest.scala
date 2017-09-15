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

package io.deepsense.deeplang.doperables.spark.wrappers.transformers

import org.apache.spark.mllib.linalg.{VectorUDT, Vectors}
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._

import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperables.Transformer
import io.deepsense.deeplang.doperables.dataframe.DataFrame

abstract class AbstractTransformerWrapperSmokeTest[+T <: Transformer]
    extends DeeplangIntegTestSupport
    with TransformerSerialization {

  import DeeplangIntegTestSupport._
  import TransformerSerialization._

  def transformerWithParams: T
  def deserializedTransformer: Transformer =
    transformerWithParams.loadSerializedTransformer(tempDir)

  final def className: String = transformerWithParams.getClass.getSimpleName

  val inputDataFrameSchema = StructType(Seq(
    StructField("as", ArrayType(StringType, containsNull = true)),
    StructField("s", StringType),
    StructField("i", IntegerType),
    StructField("i2", IntegerType),
    StructField("d", DoubleType),
    StructField("v", new VectorUDT)
  ))

  val inputDataFrame: DataFrame = {
    val rowSeq = Seq(
      Row(Array("ala", "kot", "ala"), "aAa bBb cCc", 0, 0, 0.0, Vectors.dense(0.0, 0.0, 0.0)),
      Row(Array(null), "das99213 99721 8i!#@!", 1, 1, 1.0, Vectors.dense(1.0, 1.0, 1.0))
    )
    val sparkDF = sqlContext.createDataFrame(sparkContext.parallelize(rowSeq), inputDataFrameSchema)
    DataFrame.fromSparkDataFrame(sparkDF)
  }

  className should {
    "successfully run _transform()" in {
      val transformed = transformerWithParams._transform(executionContext, inputDataFrame)
      val transformedBySerializedTransformer =
        deserializedTransformer._transform(executionContext, inputDataFrame)
      assertDataFramesEqual(transformed, transformedBySerializedTransformer)
    }
    "successfully run _transformSchema()" in {
      val transformedSchema =
        transformerWithParams._transformSchema(inputDataFrame.sparkDataFrame.schema)
      val transformedSchema2 =
        deserializedTransformer._transformSchema(inputDataFrame.sparkDataFrame.schema)
      assertSchemaEqual(transformedSchema.get, transformedSchema2.get)
    }
    "succesfully run report" in {
      val report = transformerWithParams.report
      val report2 = deserializedTransformer.report
      report shouldBe report2
    }
  }
}
