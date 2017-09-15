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

package ai.deepsense.deeplang.doperables

import java.sql.Timestamp

import ai.deepsense.sparkutils.Linalg.Vectors
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.joda.time.DateTime
import org.scalatest.Matchers
import org.scalatest.prop.GeneratorDrivenPropertyChecks

import ai.deepsense.deeplang._
import ai.deepsense.deeplang.doperables.multicolumn.MultiColumnParams.SingleOrMultiColumnChoices.SingleColumnChoice
import ai.deepsense.deeplang.doperables.multicolumn.SingleColumnParams.SingleTransformInPlaceChoices.NoInPlaceChoice
import ai.deepsense.deeplang.doperables.spark.wrappers.transformers.{DiscreteCosineTransformer, Normalizer, PolynomialExpander, TransformerSerialization}
import ai.deepsense.deeplang.params.selections._

class AutomaticNumericToVectorConversionIntegSpec
  extends DeeplangIntegTestSupport
  with GeneratorDrivenPropertyChecks
  with Matchers
  with TransformerSerialization {

  val columns = Seq(
    StructField("c", DoubleType),
    StructField("b", StringType),
    StructField("a", new ai.deepsense.sparkutils.Linalg.VectorUDT()),
    StructField("x", TimestampType),
    StructField("z", BooleanType))

  def schema: StructType = StructType(columns)

  //            "c"/0 "b"/1   "a"/2                "x"/3                                  "z"/4
  val row1 = Seq(1.1, "str1", Vectors.dense(10.0), new Timestamp(DateTime.now.getMillis), true)
  val row2 = Seq(2.2, "str2", Vectors.dense(20.0), new Timestamp(DateTime.now.getMillis), false)
  val row3 = Seq(3.3, "str3", Vectors.dense(30.0), new Timestamp(DateTime.now.getMillis), false)
  val data = Seq(row1) // , row2, row3)
  val dataFrame = createDataFrame(data.map(Row.fromSeq), schema)

  val noInPlace = NoInPlaceChoice()
    .setOutputColumn("transformed")
  val singleNoInPlace = SingleColumnChoice()
    .setInputColumn(NameSingleColumnSelection("c"))
    .setInPlace(noInPlace)
  val singleInPlace = SingleColumnChoice()
    .setInputColumn(NameSingleColumnSelection("c"))

  private def expectedInPlaceSchema(outputDataType: DataType) = schema
    .copy(schema.fields.updated(0, StructField("c", outputDataType, nullable = false)))
  private def expectedNoInPlaceSchema(outputDataType: DataType) = schema
    .copy(schema.fields.updated(0, StructField("c", DoubleType, nullable = schema("c").nullable)))
    .add(StructField("transformed", outputDataType, nullable = false))


  "Normalizer" should {
    val transformer = new Normalizer()
    transformer.set(Seq(
      transformer.p -> 1.0
    ): _*)
    "work correctly on double type column in noInPlace mode" in {
      transformer.setSingleOrMultiChoice(singleNoInPlace)
      val transformed = transformer._transform(executionContext, dataFrame)
      transformed.schema.get.treeString shouldBe expectedNoInPlaceSchema(DoubleType).treeString
    }
    "work correctly on double type column in inPlace mode" in {
      transformer.setSingleOrMultiChoice(singleInPlace)
      val transformed = transformer._transform(executionContext, dataFrame)
      transformed.schema.get.treeString shouldBe expectedInPlaceSchema(DoubleType).treeString
    }
  }

  "DiscreteCosineTransformer" should {
    val transformer = new DiscreteCosineTransformer()
    transformer.set(Seq(
      transformer.inverse -> false
    ): _*)
    "work correctly on double type column in noInPlace mode" in {
      transformer.setSingleOrMultiChoice(singleNoInPlace)
      val transformed = transformer._transform(executionContext, dataFrame)
      transformed.schema.get.treeString shouldBe expectedNoInPlaceSchema(DoubleType).treeString
    }
    "work correctly on double type column in inPlace mode" in {
      transformer.setSingleOrMultiChoice(singleInPlace)
      val transformed = transformer._transform(executionContext, dataFrame)
      transformed.schema.get.treeString shouldBe expectedInPlaceSchema(DoubleType).treeString
    }
  }

  "PolynomialExpander" should {
    val transformer = new PolynomialExpander()
    transformer.set(Seq(
      transformer.degree -> 3.0
    ): _*)
    "work correctly on double type column in noInPlace mode" in {
      transformer.setSingleOrMultiChoice(singleNoInPlace)
      val transformed = transformer._transform(executionContext, dataFrame)
      transformed.schema.get.treeString shouldBe
        expectedNoInPlaceSchema(new ai.deepsense.sparkutils.Linalg.VectorUDT).treeString
    }
    "work correctly on double type column in inPlace mode" in {
      transformer.setSingleOrMultiChoice(singleInPlace)
      val transformed = transformer._transform(executionContext, dataFrame)
      transformed.schema.get.treeString shouldBe
        expectedInPlaceSchema(new ai.deepsense.sparkutils.Linalg.VectorUDT).treeString
    }
  }
}
