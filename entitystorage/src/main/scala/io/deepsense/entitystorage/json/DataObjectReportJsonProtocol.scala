/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 *  Owner: Rafal Hryciuk
 */

package io.deepsense.entitystorage.json

import spray.httpx.SprayJsonSupport
import spray.json._

import io.deepsense.deeplang.doperables.Report
import io.deepsense.entitystorage.models.DataObjectReport


trait DataObjectReportJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {

  implicit object DataObjectJsonFormat extends JsonFormat[DataObjectReport] {

    override def write(report: DataObjectReport): JsValue = JsObject()

    override def read(json: JsValue): DataObjectReport = DataObjectReport(Report())
  }
}

object DataObjectReportJsonProtocol extends DataObjectReportJsonProtocol
