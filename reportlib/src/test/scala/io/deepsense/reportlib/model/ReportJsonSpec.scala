/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.reportlib.model

import org.scalatest.{Matchers, WordSpec}
import spray.json._

import io.deepsense.reportlib.model.factory.ReportTestFactory

class ReportJsonSpec extends WordSpec with Matchers with ReportTestFactory with ReportJsonProtocol {

  "Report" should {
    val emptyReportJson: JsObject = JsObject(
      "tables" -> JsObject(),
      "distributions" -> JsObject()
    )
    val report = testReport
    val reportJson: JsObject = JsObject(
      "tables" -> JsObject(report.tables.mapValues(_.toJson)),
      "distributions" -> JsObject(report.distributions.mapValues(_.toJson))
    )
    "serialize" when {
      "empty" in {
        val report = Report(Map(), Map())
        report.toJson shouldBe emptyReportJson
      }
      "filled report" in {
        val json = report.toJson
        json shouldBe reportJson
      }
    }
    "deserialize" when {
      "empty report" in {
        emptyReportJson.convertTo[Report] shouldBe Report(Map(), Map())
      }
      "full report" when {
        reportJson.convertTo[Report] shouldBe report
      }
    }
  }
}
