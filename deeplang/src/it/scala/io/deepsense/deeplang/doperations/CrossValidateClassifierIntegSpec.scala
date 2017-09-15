/**
 * Copyright 2015, CodiLime Inc.
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

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{BeforeAndAfter, Matchers}

import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperables.{ClassificationReporter, Report, TrainedLogisticRegression}
import io.deepsense.deeplang.parameters.{MultipleColumnSelection, NameColumnSelection, NameSingleColumnSelection}
import io.deepsense.reportlib.model.ReportJsonProtocol._

class CrossValidateClassifierIntegSpec
  extends DeeplangIntegTestSupport
  with GeneratorDrivenPropertyChecks
  with Matchers
  with Logging
  with BeforeAndAfter {

  before {
    createDir("target/tests/model")
  }

  val schema = StructType(List(
    StructField("column1", StringType),
    StructField("column2", DoubleType),
    StructField("column3", DoubleType)))

  val rows: Seq[Row] = Seq(
    Row("a", 1.0, 1.0),
    Row("b", 2.0, 2.0),
    Row("c", 3.0, 3.0),
    Row("d", 1.0, 1.0),
    Row("e", 2.0, 2.0),
    Row("f", 3.0, 3.0),
    Row("g", 1.0, 1.0),
    Row("h", 2.0, 2.0),
    Row("i", 3.0, 3.0),
    Row("j", 1.0, 1.0),
    Row("k", 2.0, 2.0),
    Row("l", 3.0, 3.0),
    Row("m", 1.0, 1.0),
    Row("n", 2.0, 2.0),
    Row("o", 3.0, 3.0),
    Row("p", 1.0, 1.0),
    Row("r", 2.0, 2.0),
    Row("s", 3.0, 3.0),
    Row("t", 1.0, 1.0),
    Row("u", 2.0, 2.0),
    Row("w", 3.0, 3.0),
    Row("x", 1.0, 1.0),
    Row("y", 2.0, 2.0),
    Row("z", 3.0, 3.0),
    Row("aa", 1.0, 1.0))

  val logisticRegression =
    CreateLogisticRegression(0, 1, 0.0001).execute(executionContext)(Vector()).head

  def createClassifier(numberOfFolds: Int) = {
    val classifier = new CrossValidateRegressor
    classifier.shuffleParameter.value = Some(CrossValidate.BinaryChoice.YES.toString)
    classifier.seedShuffleParameter.value = Some(0.0)
    classifier.numberOfFoldsParameter.value = Some(numberOfFolds)

    classifier.targetColumnParameter.value =
      Some(NameSingleColumnSelection("column3"))
    classifier.featureColumnsParameter.value =
      Some(MultipleColumnSelection(Vector(NameColumnSelection(Set("column2")))))

    classifier
  }

  "CrossValidateClassifier with parameters set" should {

    "train untrained model on DataFrame and produce report" in {
      val dataFrame = createDataFrame(rows, schema)
      val numberOfFolds = 10
      val classifier = createClassifier(numberOfFolds)

      val result = classifier.execute(executionContext)(Vector(logisticRegression, dataFrame))

      result.head.isInstanceOf[TrainedLogisticRegression] shouldBe true

      val reportContent = result.last.asInstanceOf[Report].content

      import spray.json._
      logger.debug("Cross-validation report=" + reportContent.toJson.prettyPrint)

      val summaryTable = reportContent.tables.get(ClassificationReporter.CvSummaryTableName).get

      summaryTable.rowNames.get.length shouldBe numberOfFolds
      summaryTable.values.length shouldBe numberOfFolds

      summaryTable.columnNames.get shouldBe ClassificationReporter.CvSummaryColumnNames
      summaryTable.values.foreach(
        r => r.length shouldBe ClassificationReporter.CvSummaryColumnNames.length)

      // Training and test sets sizes sum should be equal to size of DataFrame
      summaryTable.values.foreach {
        case row: List[Option[String]] =>
          row(1).get.toInt + row(2).get.toInt shouldBe rows.size
      }

      // Sum of test sets sizes from all folds should be equal to size of DataFrame
      val allTestSetsSize = summaryTable.values.fold(0) {
        case (z: Int, _ :: _ :: Some(testSetSize: String) :: _) => z + testSetSize.toInt
      }

      allTestSetsSize shouldBe rows.size
    }

    "train untrained model on DataFrame with more folds than rows" in {
      val dataFrame = createDataFrame(rows, schema)
      val numberOfFolds = 100
      val effectiveNumberOfFolds = rows.size
      val classifier = createClassifier(numberOfFolds)

      val result = classifier.execute(executionContext)(Vector(logisticRegression, dataFrame))

      result.head.isInstanceOf[TrainedLogisticRegression] shouldBe true

      val reportContent = result.last.asInstanceOf[Report].content

      import spray.json._
      logger.debug("Cross-validation report=" + reportContent.toJson.prettyPrint)

      val summaryTable = reportContent.tables.get(ClassificationReporter.CvSummaryTableName).get

      summaryTable.rowNames.get.length shouldBe effectiveNumberOfFolds
      summaryTable.values.length shouldBe effectiveNumberOfFolds
    }

    "train untrained model on DataFrame with one row and an empty report" in {
      val dataFrame = createDataFrame(rows.slice(0, 1), schema)
      val numberOfFolds = 100  // this number won't matter, since there is only one row in the DF
      val classifier = createClassifier(numberOfFolds)

      val result = classifier.execute(executionContext)(Vector(logisticRegression, dataFrame))

      result.head.isInstanceOf[TrainedLogisticRegression] shouldBe true

      val reportContent = result.last.asInstanceOf[Report].content

      import spray.json._
      logger.debug("Cross-validation report=" + reportContent.toJson.prettyPrint)

      reportContent.tables.isEmpty shouldBe true
    }
  }
}
