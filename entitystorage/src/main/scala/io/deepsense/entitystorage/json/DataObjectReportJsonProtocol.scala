/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 *  Owner: Rafal Hryciuk
 */

package io.deepsense.entitystorage.json

import spray.httpx.SprayJsonSupport
import spray.json._

import io.deepsense.deeplang.doperables.Report
import io.deepsense.models.entities.DataObjectReport


trait DataObjectReportJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {

  implicit object DataObjectJsonFormat extends JsonFormat[DataObjectReport] {

    override def write(report: DataObjectReport): JsValue = JsString(report.report.message)

    override def read(json: JsValue): DataObjectReport = json match {
      case JsString(value) => DataObjectReport(Report(value))
      case x => throw new DeserializationException(s"Invalid DataObjectReport in JSON: $x")
    }
  }
}

object DataObjectReportJsonProtocol extends DataObjectReportJsonProtocol
