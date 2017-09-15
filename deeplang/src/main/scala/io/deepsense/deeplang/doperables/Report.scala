/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.deeplang.doperables

import spray.json._

import io.deepsense.deeplang.{ExecutionContext, DOperable}
import io.deepsense.models.entities.DataObjectReport
import io.deepsense.reportlib.model.{ReportContent, ReportJsonProtocol}

case class Report(content: ReportContent = ReportContent("empty report"))
  extends DOperable {

  def this() = this(ReportContent("empty report"))

  import ReportJsonProtocol._
  override def report: Report = this

  def toDataObjectReport: DataObjectReport = DataObjectReport(content.toJson.prettyPrint)

  override def save(context: ExecutionContext)(path: String): Unit = ???
}
