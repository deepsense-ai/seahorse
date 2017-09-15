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

package io.deepsense.deeplang.doperables

import spray.json._

import io.deepsense.deeplang.{ExecutionContext, DOperable}
import io.deepsense.models.entities.DataObjectReport
import io.deepsense.reportlib.model.{ReportContent, ReportJsonProtocol}

case class Report(content: ReportContent = ReportContent("empty report"))
  extends DOperable {

  def this() = this(ReportContent("empty report"))

  override def toInferrable: DOperable = new Report()

  import ReportJsonProtocol._
  override def report: Report = this

  def toDataObjectReport: DataObjectReport = DataObjectReport(content.toJson.prettyPrint)

  override def save(context: ExecutionContext)(path: String): Unit = ???
}
