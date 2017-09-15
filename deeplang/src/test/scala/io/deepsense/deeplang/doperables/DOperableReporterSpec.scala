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

package io.deepsense.deeplang.doperables

import org.apache.spark.mllib.linalg.Vectors
import org.mockito.Mockito._

import io.deepsense.commons.types.ColumnType
import io.deepsense.commons.utils.DoubleUtils
import io.deepsense.deeplang.UnitSpec

class DOperableReporterSpec extends UnitSpec {

  val reportName = "Test Report Name"
  val description = "Test Description"

  "DOperableReporter" should {

    "create report" when {

      "no parameters are passed" in {

        val report = DOperableReporter(reportName)
          .report()

        report.content should not be null
        report.content.name shouldBe reportName
        report.content.tables shouldBe Map.empty
      }

      "parameters are passed" in {

        val parameters = Seq(
          ("Column 1", ColumnType.string, "Value 1"),
          ("Column 2", ColumnType.categorical, "Value 2"),
          ("Column 3", ColumnType.numeric, "3.0")
        )

        val report = DOperableReporter(reportName)
          .withParameters(
            description,
            parameters: _*
          )
          .report()

        report.content should not be null
        report.content.name shouldBe reportName
        report.content.tables should not be Map.empty
        val table = report.content.tables("Parameters")
        table.columnNames shouldBe Some(List(
          "Column 1", "Column 2", "Column 3"))
        table.values shouldBe List(
          List(Some("Value 1"), Some("Value 2"), Some("3.0")))
      }

      "feature and target columns are passed" in {

        val operable = mock[VectorScoring]
        when(operable.featureColumns).thenReturn(Seq("column1", "column2"))
        when(operable.targetColumn).thenReturn("target")

        val report = DOperableReporter(reportName)
          .withVectorScoring(operable)
          .report()

        report.content should not be null
        report.content.name shouldBe reportName
        report.content.tables should not be Map.empty
        val featuresTable = report.content.tables("Feature columns")
        featuresTable.columnNames shouldBe Some(List(
          "Feature columns"))
        featuresTable.values shouldBe List(
          List(Some("column1")), List(Some("column2")))
        val targetTable = report.content.tables("Target column")
        targetTable.columnNames shouldBe Some(List(
          "Target column"))
        targetTable.values shouldBe List(
          List(Some("target")))
      }

      "coefficients are passed" in {

        val weights = Vectors.dense(1.0, 2.0, 3.0)
        val intercept = 4.0

        val report = DOperableReporter(reportName)
          .withCoefficients(
            description,
            weights,
            intercept
          )
          .report()

        report.content should not be null
        report.content.name shouldBe reportName
        report.content.tables should not be Map.empty
        val table = report.content.tables("Coefficients")
        table.columnNames shouldBe Some(List(
          "Weights", "Intercept"))
        table.values shouldBe List(
          List(Some(DoubleUtils.double2String(1.0)), Some(DoubleUtils.double2String(4.0))),
          List(Some(DoubleUtils.double2String(2.0)), Some("")),
          List(Some(DoubleUtils.double2String(3.0)), Some(""))
        )
      }

      "custom parameters are passed" in {

        val parameters = Seq(
          ("Column 1", ColumnType.string, Seq("Value 11", "Value 12", "Value 13")),
          ("Column 2", ColumnType.categorical, Seq("Value 21", "Value 22")),
          ("Column 3", ColumnType.numeric, Seq("3.0"))
        )

        val report = DOperableReporter(reportName)
          .withCustomTable(
            "name",
            description,
            parameters: _*
          )
          .report()

        report.content should not be null
        report.content.name shouldBe reportName
        report.content.tables should not be Map.empty
        val table = report.content.tables("name")
        table.columnNames shouldBe Some(List(
          "Column 1", "Column 2", "Column 3"))
        table.values shouldBe List(
          List(Some("Value 11"), Some("Value 21"), Some("3.0")),
          List(Some("Value 12"), Some("Value 22"), Some("")),
          List(Some("Value 13"), Some(""), Some(""))
        )
      }
    }
  }
}
