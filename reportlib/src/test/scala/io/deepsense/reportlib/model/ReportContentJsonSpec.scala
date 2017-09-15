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

package io.deepsense.reportlib.model

import io.deepsense.reportlib.model.factory.ReportContentTestFactory
import org.scalatest.{Matchers, WordSpec}
import spray.json._

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
      "distributions" -> JsObject(),
      "schema" -> JsNull
    )
    val report = testReport
    val reportJson: JsObject = JsObject(
      "name" -> JsString(reportName),
      "tables" -> JsObject(report.tables.mapValues(_.toJson)),
      "distributions" -> JsObject(report.distributions.mapValues(_.toJson)),
      "schema" -> JsObject(
        "fields" -> JsArray(
          JsObject(
            "name" -> JsString("x"),
            "dataType" -> JsString("integer"),
            "deeplangType" -> JsString("numeric"),
            "nullable" -> JsTrue
          ),
          JsObject(
            "name" -> JsString("y"),
            "dataType" -> JsString("double"),
            "deeplangType" -> JsString("numeric"),
            "nullable" -> JsFalse
          )
        )
      )
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
