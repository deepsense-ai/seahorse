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

import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperables.Transformer
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.params.ParamPair
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StringType, StructField, StructType}

abstract class AbstractTransformerWrapperSmokeTest extends DeeplangIntegTestSupport {

  def className: String

  val transformer: Transformer

  val transformerParams: Seq[ParamPair[_]]

  val inputDataFrameSchema = StructType(Seq(
    StructField("s", StringType)
  ))

  val inputDataFrame: DataFrame = {
    val rowSeq = Seq(
      Row("aAa bBb cCc"),
      Row("das99213 99721 8i!#@!")
    )
    val sparkDF = sqlContext.createDataFrame(sparkContext.parallelize(rowSeq), inputDataFrameSchema)
    DataFrame.fromSparkDataFrame(sparkDF)
  }

  className should {
    "successfully run _transform()" in {
      transformer.set(transformerParams: _*)._transform(executionContext, inputDataFrame)
    }
    "successfully run _transformSchema()" in {
      transformer.set(transformerParams: _*)._transformSchema(inputDataFrame.sparkDataFrame.schema)
    }
  }
}
