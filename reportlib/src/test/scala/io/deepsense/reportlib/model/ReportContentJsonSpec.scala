/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.reportlib.model

import org.scalatest.{Matchers, WordSpec}
import spray.json._

import io.deepsense.reportlib.model.factory.ReportContentTestFactory

class ReportContentJsonSpec
  extends WordSpec
  with Matchers
  with ReportContentTestFactory
  with ReportJsonProtocol {

  "ReportContent" should {
    val reportName = ReportContentTestFactory.reportContentName
    val emptyReportJson: JsObject = JsObject(
      "name" -> JsString(reportName),
      "tables" -> JsObject(),
      "distributions" -> JsObject()
    )
    val report = testReport
    val reportJson: JsObject = JsObject(
      "name" -> JsString(reportName),
      "tables" -> JsObject(report.tables.mapValues(_.toJson)),
      "distributions" -> JsObject(report.distributions.mapValues(_.toJson))
    )
    "serialize" when {
      "empty" in {
        val report = ReportContent(reportName, Map(), Map())
        report.toJson shouldBe emptyReportJson
      }
      "filled report" in {
        val json = report.toJson
        json shouldBe reportJson
      }
    }
    "deserialize" when {
      "empty report" in {
        emptyReportJson.convertTo[ReportContent] shouldBe ReportContent(reportName, Map(), Map())
      }
      "full report" when {
        reportJson.convertTo[ReportContent] shouldBe report
      }
    }
  }
}
