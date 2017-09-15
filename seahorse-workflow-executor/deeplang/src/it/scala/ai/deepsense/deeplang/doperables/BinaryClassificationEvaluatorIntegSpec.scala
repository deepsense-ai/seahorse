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

import ai.deepsense.sparkutils.Linalg.Vectors
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._

import ai.deepsense.deeplang.doperables.spark.wrappers.evaluators.BinaryClassificationEvaluator
import ai.deepsense.deeplang.doperables.spark.wrappers.evaluators.BinaryClassificationEvaluator._
import ai.deepsense.deeplang.doperations.exceptions._
import ai.deepsense.deeplang.params.selections.NameSingleColumnSelection
import ai.deepsense.deeplang.{DKnowledge, DeeplangIntegTestSupport}

class BinaryClassificationEvaluatorIntegSpec extends DeeplangIntegTestSupport {

  "BinaryClassificationEvaluator" should {
    val eval = new BinaryClassificationEvaluator()

    def simpleSchema: StructType = StructType(Seq(
      StructField("label", DoubleType),
      StructField("prediction", DoubleType),
      StructField("rawPrediction", new ai.deepsense.sparkutils.Linalg.VectorUDT())))
    val simpleData = Seq(
      Seq(0.0, 1.0, Vectors.dense(-0.001, 0.001)),
      Seq(1.0, 1.0, Vectors.dense(-0.001, 0.001)),
      Seq(1.0, 1.0, Vectors.dense(-15.261485, 15.261485))
    )
    val simpleDataFrame = createDataFrame(simpleData.map(Row.fromSeq), simpleSchema)

    "calculate correct values for simple example" in {
      val areaUnderROC =
        eval.setMetricName(new AreaUnderROC()).evaluate(executionContext)()(simpleDataFrame)
      areaUnderROC.value shouldBe 3.0 / 4.0
      val areaUnderPR =
        eval.setMetricName(new AreaUnderPR()).evaluate(executionContext)()(simpleDataFrame)
      areaUnderPR.value shouldBe 11.0 / 12.0
      val precision =
        eval.setMetricName(new Precision()).evaluate(executionContext)()(simpleDataFrame)
      precision.value shouldBe 2.0 / 3.0
      val recall = eval.setMetricName(new Recall()).evaluate(executionContext)()(simpleDataFrame)
      recall.value shouldBe 1.0
      val f1Score = eval.setMetricName(new F1Score()).evaluate(executionContext)()(simpleDataFrame)
      // F1-score calculation formula available at: https://en.wikipedia.org/wiki/F1_score
      f1Score.value shouldBe 0.8
    }


    // Based on "recognizing dogs in scenes from a video" example in:
    // https://en.wikipedia.org/wiki/Precision_and_recall
    def dogSchema: StructType = StructType(Seq(
      StructField("label", DoubleType),
      StructField("prediction", DoubleType)))
    val dogData = Seq(
      Seq(1.0, 1.0),
      Seq(1.0, 1.0),
      Seq(1.0, 1.0),
      Seq(1.0, 1.0),
      Seq(0.0, 1.0),
      Seq(0.0, 1.0),
      Seq(0.0, 1.0),
      Seq(1.0, 0.0),
      Seq(1.0, 0.0),
      Seq(1.0, 0.0),
      Seq(1.0, 0.0),
      Seq(1.0, 0.0)
    )
    val dogDataFrame = createDataFrame(dogData.map(Row.fromSeq), dogSchema)

    "calculate correct precision, recall and f1-score values" in {
      val precision = eval.setMetricName(new Precision()).evaluate(executionContext)()(dogDataFrame)
      precision.value shouldBe 4.0 / 7.0
      val recall = eval.setMetricName(new Recall()).evaluate(executionContext)()(dogDataFrame)
      recall.value shouldBe 4.0 / 9.0
      val f1Score = eval.setMetricName(new F1Score()).evaluate(executionContext)()(dogDataFrame)
      // F1-score calculation formula available at: https://en.wikipedia.org/wiki/F1_score
      f1Score.value shouldBe 0.5
    }

    "calculate correct precision, recall and f1-score values with non-default column names" in {
      def dogSchemaRenamed: StructType = StructType(Seq(
        StructField("labe", DoubleType),
        StructField("pred", DoubleType)))
      val dogDFRenamed = createDataFrame(dogData.map(Row.fromSeq), dogSchemaRenamed)

      val evalRenamed = new BinaryClassificationEvaluator()
      evalRenamed.setLabelColumn(new NameSingleColumnSelection("labe"))
      val predSel = new NameSingleColumnSelection("pred")
      val precision = evalRenamed.setMetricName(new Precision().setPredictionColumnParam(predSel))
        .evaluate(executionContext)()(dogDFRenamed)
      precision.value shouldBe 4.0 / 7.0
      val recall = evalRenamed.setMetricName(new Recall().setPredictionColumnParam(predSel))
        .evaluate(executionContext)()(dogDFRenamed)
      recall.value shouldBe 4.0 / 9.0
      val f1Score = evalRenamed.setMetricName(new F1Score().setPredictionColumnParam(predSel))
        .evaluate(executionContext)()(dogDFRenamed)
      // F1-score calculation formula from: https://en.wikipedia.org/wiki/F1_score
      f1Score.value shouldBe 2.0 * precision.value * recall.value / (precision.value + recall.value)
    }

    "throw exceptions" when {
      // TODO: Schema cannot be checked in method _infer until DS-3258 is fixed
      // currently schema is checked only in method evaluate
      "label column has invalid type" in {
        def invalidSchema: StructType = StructType(Seq(
          StructField("label", StringType),
          StructField("prediction", DoubleType)))
        // TODO: DS-3258: Creating DF with dummy data should not be necessary for _infer
        // DataFrame.forInference(invalidSchema) should be sufficient
        val invalidDataFrame = createDataFrame(Seq(Row("label1", 1.0)), invalidSchema)

        intercept[WrongColumnTypeException] {
          eval.setMetricName(new Precision())._infer(DKnowledge(invalidDataFrame))
        }
        ()
      }

      "prediction column has invalid type and there is no rawPrediction column" in {
        def invalidSchema: StructType = StructType(Seq(
          StructField("label", DoubleType),
          StructField("prediction", StringType)))
        // TODO: DS-3258: Creating DF with dummy data should not be necessary for _infer
        // DataFrame.forInference(invalidSchema) should be sufficient
        val invalidDataFrame = createDataFrame(Seq(Row(0.0, "pred1")), invalidSchema)

        intercept[ColumnDoesNotExistException] {
          eval.setMetricName(new AreaUnderROC())._infer(DKnowledge(invalidDataFrame))
        }
        intercept[ColumnDoesNotExistException] {
          eval.setMetricName(new AreaUnderPR())._infer(DKnowledge(invalidDataFrame))
        }
        intercept[WrongColumnTypeException] {
          eval.setMetricName(new Precision())._infer(DKnowledge(invalidDataFrame))
        }
        intercept[WrongColumnTypeException] {
          eval.setMetricName(new Recall())._infer(DKnowledge(invalidDataFrame))
        }
        intercept[WrongColumnTypeException] {
          eval.setMetricName(new F1Score())._infer(DKnowledge(invalidDataFrame))
        }
        ()
      }

      "rawPrediction column has invalid type and there is no prediction column" in {
        def invalidSchema: StructType = StructType(Seq(
          StructField("label", DoubleType),
          StructField("rawPrediction", DoubleType)))  // rawPrediction should be vector column
        // TODO: DS-3258: Creating DF with dummy data should not be necessary for _infer
        // DataFrame.forInference(invalidSchema) should be sufficient
        val invalidDataFrame = createDataFrame(Seq(Row(0.0, 1.0)), invalidSchema)

        intercept[WrongColumnTypeException] {
          eval.setMetricName(new AreaUnderROC())._infer(DKnowledge(invalidDataFrame))
        }
        intercept[WrongColumnTypeException] {
          eval.setMetricName(new AreaUnderPR())._infer(DKnowledge(invalidDataFrame))
        }
        intercept[ColumnDoesNotExistException] {
          eval.setMetricName(new Precision())._infer(DKnowledge(invalidDataFrame))
        }
        intercept[ColumnDoesNotExistException] {
          eval.setMetricName(new Recall())._infer(DKnowledge(invalidDataFrame))
        }
        intercept[ColumnDoesNotExistException] {
          eval.setMetricName(new F1Score())._infer(DKnowledge(invalidDataFrame))
        }
        ()
      }
    }
  }
}
