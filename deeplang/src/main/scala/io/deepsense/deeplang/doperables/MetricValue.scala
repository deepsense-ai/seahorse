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

import io.deepsense.commons.types.ColumnType
import io.deepsense.commons.utils.DoubleUtils
import io.deepsense.deeplang.DOperable
import io.deepsense.deeplang.doperables.report.Report
import io.deepsense.reportlib.model.{ReportType, Table}

/**
  * Metric value.
  *
  * @param name name of the metric (e.g. RMSE).
  * @param value value.
  */
case class MetricValue(name: String, value: Double) extends DOperable {

  def this() = this(null, Double.NaN)



  override def report: Report =
    super.report
      .withReportName("Report for Metric Value")
      .withReportType(ReportType.MetricValue)
      .withAdditionalTable(
        Table(
          name = "Metric Value",
          description = "",
          columnNames = Some(List(name)),
          columnTypes = List(ColumnType.string),
          rowNames = None,
          values = List(List(Some(DoubleUtils.double2String(value))))))
}

object MetricValue {

  def forInference(name: String): MetricValue = MetricValue(name, Double.NaN)
}
