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

import org.apache.spark.sql.types.{DoubleType, StructField, StructType}

import ai.deepsense.deeplang.doperables.MultiColumnTransformerIntegSpec._
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperables.multicolumn.MultiColumnParams.MultiColumnInPlaceChoices.{MultiColumnNoInPlace, MultiColumnYesInPlace}
import ai.deepsense.deeplang.doperables.multicolumn.MultiColumnParams.SingleOrMultiColumnChoices.{MultiColumnChoice, SingleColumnChoice}
import ai.deepsense.deeplang.doperables.multicolumn.SingleColumnParams.SingleTransformInPlaceChoices.{NoInPlaceChoice, YesInPlaceChoice}
import ai.deepsense.deeplang.inference.InferContext
import ai.deepsense.deeplang.params.selections.{MultipleColumnSelection, NameColumnSelection, NameSingleColumnSelection}
import ai.deepsense.deeplang.params.{NumericParam, Param}
import ai.deepsense.deeplang.{DKnowledge, DeeplangIntegTestSupport, ExecutionContext}


class MultiColumnTransformerIntegSpec extends DeeplangIntegTestSupport {

  val magicConstant: Double = 1337d
  import DeeplangIntegTestSupport._

  "MultiColumnTransformer" should {
    "return also operation specific params in json" in {
      val t: AddAConstantTransformer = transformerWithMagicConstant
      t.params should contain (t.magicConstant)
    }
  }
  "MultiColumnTransformer" when {
    "working with multiple columns" when {
      val t = transformerWithMagicConstant
      t.setMultipleColumns(columns = Seq("x", "y"), inPlace = Some("magic_"))
      "in-place mode was not selected" should {
        "create columns with unique name (with prefix)" in {
          val transformedInPlace = t.transform(executionContext)(())(inputData)
          transformedInPlace.schema shouldBe expectedTransformedMulti.schema
          assertDataFramesEqual(transformedInPlace, expectedTransformedMulti)
        }
        "infer schema for columns with unique name (with prefix)" in {
          val (k, _) = t.transform
            .infer(mock[InferContext])(())(DKnowledge(DataFrame.forInference(inputSchema.get)))
          assertSchemaEqual(
            k.single.schema.get,
            expectedTransformedMulti.schema.get,
            checkNullability = false)
        }
      }
      "in-place mode was selected" should {
        val t = transformerWithMagicConstant
        t.setMultipleColumns(columns = Seq("x", "y"), inPlace = None)
        "replace columns" in {
          val transformedInPlace = t.transform(executionContext)(())(inputData)
          assertDataFramesEqual(transformedInPlace, expectedMultiInPlace)
        }
        "replace columns schema" in {
          val (k, _) = t.transform
            .infer(mock[InferContext])(())(DKnowledge(DataFrame.forInference(inputSchema.get)))
          assertSchemaEqual(
            k.single.schema.get,
            expectedMultiInPlace.schema.get,
            checkNullability = false)
        }
      }
    }
    "working with a single column" should {
      val t = transformerWithMagicConstant
      t.setSingleColumn(column = "y", inPlace = Some("updatedy"))
      "in-place mode was not selected" when {
        "create a new column" in {
          val transformedY = t.transform(executionContext)(())(inputData)
          transformedY.schema shouldBe expectedTransformedY.schema
          assertDataFramesEqual(transformedY, expectedTransformedY)
        }
        "infer schema for columns with unique name (with prefix)" in {
          val (k, _) = t.transform
            .infer(mock[InferContext])(())(DKnowledge(DataFrame.forInference(inputSchema.get)))
          assertSchemaEqual(
            k.single.schema.get,
            expectedTransformedY.schema.get,
            checkNullability = false)
        }
      }
      "in-place mode was selected" when {
        val t = transformerWithMagicConstant
        t.setSingleColumn(column = "y", inPlace = None)
        "replace a column" in {
          val transformedY = t.transform(executionContext)(())(inputData)
          transformedY.schema shouldBe expectedTransformedYInPlace.schema
          assertDataFramesEqual(transformedY, expectedTransformedYInPlace)
        }
        "replace columns schema" in {
          val (k, _) = t.transform
            .infer(mock[InferContext])(())(DKnowledge(DataFrame.forInference(inputSchema.get)))
          assertSchemaEqual(
            k.single.schema.get,
            expectedTransformedYInPlace.schema.get,
            checkNullability = false)
        }
      }
    }
  }

  def transformerWithMagicConstant: AddAConstantTransformer = {
    val t = AddAConstantTransformer()
    t.setMagicConstant(magicConstant)
    t
  }

  val rawInputData = Seq(
    InputData(3, "abc", 5, 23),
    InputData(14, "def", 5, 4),
    InputData(15, "ghi", 5, 89),
    InputData(29, "jkl", 5, 13))

  val inputData = createDataFrame(rawInputData)

  val inputSchema = inputData.schema

  val expectedMultiInPlace = createDataFrame(
    rawInputData.map( d => InputDataDouble(d.x + magicConstant, d.a, d.y + magicConstant, d.z)))

  val expectedTransformedYInPlace = createDataFrame(
    rawInputData.map( d => InputDataDouble(d.x, d.a, d.y + magicConstant, d.z)))

  val expectedTransformedY = createDataFrame(
    rawInputData.map(d => InputDataUpdatedY(d.x, d.a, d.y, d.z, d.y + magicConstant)))

  val expectedTransformedMulti = createDataFrame(rawInputData.map { d =>
    InputDataUpdatedMulti(d.x, d.a, d.y, d.z, d.x + magicConstant, d.y + magicConstant)
  })
}

object MultiColumnTransformerIntegSpec {

  case class InputData(x: Double, a: String, y: Int, z: Double)
  case class InputDataDouble(x: Double, a: String, y: Double, z: Double)
  case class InputDataUpdatedY(x: Double, a: String, y: Int, z: Double, updatedy: Double)
  case class InputDataUpdatedMulti(
    x: Double, a: String, y: Int, z: Double, magic_x: Double, magic_y: Double)

  case class AddAConstantTransformer() extends MultiColumnTransformer {

    val magicConstant = NumericParam(
      name = "aconstant",
      description = Some("Constant that will be added to columns")
    )

    def setMagicConstant(value: Double): this.type = set(magicConstant, value)

    override def getSpecificParams: Array[Param[_]] = Array(magicConstant)

    override def transformSingleColumn(
        inputColumn: String,
        outputColumn: String,
        context: ExecutionContext,
        dataFrame: DataFrame): DataFrame = {

      transformSingleColumnSchema(inputColumn, outputColumn, dataFrame.sparkDataFrame.schema)
      val magicConstantValue = $(magicConstant)
      DataFrame.fromSparkDataFrame(
        dataFrame.sparkDataFrame.selectExpr(
          "*",
          s"cast(`$inputColumn` as double) + $magicConstantValue as `$outputColumn`"))
    }

    override def transformSingleColumnSchema(
        inputColumn: String,
        outputColumn: String,
        schema: StructType): Option[StructType] = {
      if (schema.fieldNames.contains(outputColumn)) {
        throw new IllegalArgumentException(s"Output column $outputColumn already exists.")
      }
      val outputFields = schema.fields :+
        StructField(outputColumn, DoubleType, nullable = false)
      Some(StructType(outputFields))
    }

    def setSingleColumn(column: String, inPlace: Option[String]): this.type = {
      val inplaceChoice = inPlace match {
        case Some(x) => NoInPlaceChoice().setOutputColumn(x)
        case None => YesInPlaceChoice()
      }

      val single = SingleColumnChoice()
        .setInputColumn(NameSingleColumnSelection(column))
        .setInPlace(inplaceChoice)

      setSingleOrMultiChoice(single)
    }

    def setMultipleColumns(columns: Seq[String], inPlace: Option[String]): this.type = {
      val inPlaceChoice = inPlace match {
        case Some(x) => MultiColumnNoInPlace().setColumnsPrefix(x)
        case None => MultiColumnYesInPlace()
      }

      val columnSelection = NameColumnSelection(columns.toSet)
      val multiple = MultiColumnChoice()
        .setInputColumnsParam(MultipleColumnSelection(Vector(columnSelection)))
        .setMultiInPlaceChoice(inPlaceChoice)

      setSingleOrMultiChoice(multiple)
    }
  }
}
