/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright 2018 Astraea. Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     [http://www.apache.org/licenses/LICENSE-2.0]
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package ai.deepsense.deeplang.doperations

import ai.deepsense.deeplang.DOperation.Id
import ai.deepsense.deeplang.{DOperation1To1, ExecutionContext}
import ai.deepsense.deeplang.doperables.dataframe.DataFrame
import ai.deepsense.deeplang.doperables.dataframe.report.DataFrameReportGenerator
import ai.deepsense.deeplang.doperables.report.Report
import ai.deepsense.deeplang.doperations.DataFrameReport.{ReportTypeChoice, SchemaOnly}
import ai.deepsense.deeplang.params.Param
import ai.deepsense.deeplang.params.choice.{Choice, ChoiceParam}
import org.apache.spark.sql

import scala.reflect.runtime.universe._

/**
  * Generates DataFrame reports from a menu of options.
  *
  * @since 5/1/18
  */
class DataFrameReport extends DOperation1To1[DataFrame, Report] {
  override val id: Id = "a38bb4ca-a0be-4689-a17b-786e5ea09fee"
  override val name: String = "DataFrame Report"
  override val description: String = "Generates summarization report on given DataFrame."

  override def params: Array[Param[_]] = Array(format)

  override def tTagTI_0: TypeTag[DataFrame] = implicitly
  override def tTagTO_0: TypeTag[Report] = implicitly

  val format = ChoiceParam[ReportTypeChoice](
    "Report type",
    Some("Selects the type of report computed, from simple to most compute intensive.")
  )
  setDefault(format, SchemaOnly())

  override protected def execute(t0: DataFrame)(context: ExecutionContext): Report =
    $(format).generate(t0.sparkDataFrame)
}

object DataFrameReport {
  sealed trait ReportTypeChoice extends Choice {
    override val choiceOrder: List[Class[_ <: Choice]] = List(
      classOf[SchemaOnly],
      classOf[CountAndSchema],
      classOf[FullSummaryStatistics]
    )
    override def params: Array[Param[_]] = Array.empty
    def generate(sparkDataFrame: sql.DataFrame): Report
  }

  case class SchemaOnly() extends ReportTypeChoice {
    override val name: String = "Schema only"
    override def generate(sparkDataFrame: sql.DataFrame) =
      DataFrameReportGenerator.minimalReport(sparkDataFrame)
  }

  case class CountAndSchema() extends ReportTypeChoice {
    override val name: String = "Count and schema"
    override def generate(sparkDataFrame: sql.DataFrame) =
      DataFrameReportGenerator.simplifiedReport(sparkDataFrame)
  }

  case class FullSummaryStatistics() extends ReportTypeChoice {
    override val name: String = "Full summary statistics and data sample"
    override def generate(sparkDataFrame: sql.DataFrame) =
      DataFrameReportGenerator.fullReport(sparkDataFrame)
  }
}
