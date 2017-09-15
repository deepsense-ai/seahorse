/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 *  Owner: Rafal Hryciuk
 */

package io.deepsense.deeplang.doperables

import spray.json._

import io.deepsense.deeplang.DOperable
import io.deepsense.models.entities.DataObjectReport
import io.deepsense.reportlib.model.{ReportContent, ReportJsonProtocol}

case class Report(content: ReportContent = ReportContent("empty report"))
  extends DOperable {
  import ReportJsonProtocol._
  override def report: Report = this

  def toDataObjectReport: DataObjectReport = DataObjectReport(content.toJson.prettyPrint)
}
