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

package ai.deepsense.deeplang.doperables.report

import ai.deepsense.deeplang.DOperable
import ai.deepsense.reportlib.model.ReportType.ReportType
import ai.deepsense.reportlib.model.{ReportContent, ReportType, Table}

case class Report(content: ReportContent = ReportContent("Empty Report", ReportType.Empty))
  extends DOperable {

  def this() = this(ReportContent("Empty Report", ReportType.Empty))

  def withReportName(newName: String): Report =
    copy(content.copy(name = newName))

  def withReportType(newReportType: ReportType): Report =
    copy(content.copy(reportType = newReportType))

  def withAdditionalTable(table: Table, at: Int = 0): Report = {
    require(at <= content.tables.length && at >= 0,
      s"Table can be placed in possible position: [0; ${content.tables.length}]")
    val (left, right) = content.tables.splitAt(at)
    val newTables = left ++ Seq(table) ++ right
    copy(content.copy(tables = newTables))
  }

  override def report(extended: Boolean = true): Report = this
}
