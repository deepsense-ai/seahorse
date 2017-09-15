package io.deepsense.deeplang.doperables

import org.apache.spark.mllib.classification.{LogisticRegressionModel, LogisticRegressionWithLBFGS}
import org.apache.spark.mllib.linalg
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.BeforeAndAfter

import io.deepsense.deeplang.doperations.exceptions.{ColumnDoesNotExistException, ColumnsDoNotExistException, WrongColumnTypeException}
import io.deepsense.deeplang.parameters.{MultipleColumnSelection, NameColumnSelection, NameSingleColumnSelection}
import io.deepsense.deeplang.{DeeplangIntegTestSupport, ExecutionContext}

class UntrainedLogisticRegressionIntegSpec extends DeeplangIntegTestSupport with BeforeAndAfter {

  "UntrainedLogisticRegression" should {

    val inputRows: Seq[Row] = Seq(
      Row(-2.0, "x", -2.22, 1000.0),
      Row(-2.0, "y", -22.2, 2000.0),
      Row(-2.0, "z", -2222.0, 6000.0))

    val inputSchema: StructType = StructType(Seq(
      StructField("column1", DoubleType),
      StructField("column2", StringType),
      StructField("column3", DoubleType),
      StructField("column0", DoubleType))
    )

    lazy val inputDataFrame = createDataFrame(inputRows, inputSchema)

    val mockUntrainedModel = mock[LogisticRegressionWithLBFGS]

    "create trained model on given DataFrame" in {
      val mockContext: ExecutionContext = mock[ExecutionContext]
      val mockTrainedModel = mock[LogisticRegressionModel]

      when(mockUntrainedModel.run(any[RDD[LabeledPoint]]())).thenAnswer(
        new Answer[LogisticRegressionModel] {
          override def answer(invocationOnMock: InvocationOnMock) = {
            val receivedRDD = invocationOnMock.getArgumentAt(0, classOf[RDD[LabeledPoint]])
            val collected: Array[LabeledPoint] = receivedRDD.collect()
            val allLabels = collected.map(_.label)
            allLabels shouldBe Seq(-2.22, -22.2, -2222.0)
            val features: Array[linalg.Vector] = collected.map(_.features)
            features(0)(0) shouldBe -2.0
            features(0)(1) shouldBe 1000.0
            features(1)(0) shouldBe -2.0
            features(1)(1) shouldBe 2000.0
            features(2)(0) shouldBe -2.0
            features(2)(1) shouldBe 6000.0
            mockTrainedModel
          }
        })

      val regression = UntrainedLogisticRegression(Some(mockUntrainedModel))
      val parameters = Trainable.Parameters(
        featureColumns = MultipleColumnSelection(
          Vector(NameColumnSelection(Set("column0", "column1")))),
        targetColumn = NameSingleColumnSelection("column3"))

      val result = regression.train(mockContext)(parameters)(inputDataFrame)
      val castedResult = result.asInstanceOf[TrainedLogisticRegression]
      castedResult.model shouldBe Some(mockTrainedModel)
      castedResult.featureColumns shouldBe Some(Seq("column1", "column0"))
      castedResult.targetColumn shouldBe Some("column3")
    }

    "throw an exception" when {
      "non-existing column was selected as target" in {
        intercept[ColumnDoesNotExistException] {
          val regression = UntrainedLogisticRegression(Some(mockUntrainedModel))
          val parameters = Trainable.Parameters(
            featureColumns = MultipleColumnSelection(
              Vector(NameColumnSelection(Set("column0", "column1")))),
            targetColumn = NameSingleColumnSelection("not exists"))
          regression.train(executionContext)(parameters)(inputDataFrame)
        }
      }
      "non-existing columns was selected as features" in {
        intercept[ColumnsDoNotExistException] {
          val regression = UntrainedLogisticRegression(Some(mockUntrainedModel))
          val parameters = Trainable.Parameters(
            featureColumns = MultipleColumnSelection(
              Vector(NameColumnSelection(Set("not exists", "column1")))),
            targetColumn = NameSingleColumnSelection("column3"))
          regression.train(executionContext)(parameters)(inputDataFrame)
        }
      }
      "not all selected features were Double" in {
        intercept[WrongColumnTypeException] {
          val regression = UntrainedLogisticRegression(Some(mockUntrainedModel))
          val parameters = Trainable.Parameters(
            featureColumns = MultipleColumnSelection(
              Vector(NameColumnSelection(Set("column2", "column1")))),
            targetColumn = NameSingleColumnSelection("column3"))
          regression.train(executionContext)(parameters)(inputDataFrame)
        }
      }
      "selected target was not Double" in {
        intercept[WrongColumnTypeException] {
          val regression = UntrainedLogisticRegression(Some(mockUntrainedModel))
          val parameters = Trainable.Parameters(
            featureColumns = MultipleColumnSelection(
              Vector(NameColumnSelection(Set("column0", "column1")))),
            targetColumn = NameSingleColumnSelection("column2"))
          regression.train(executionContext)(parameters)(inputDataFrame)
        }
      }
    }
  }
}
