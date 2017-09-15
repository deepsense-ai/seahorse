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

package ai.deepsense.reportlib.model

import ai.deepsense.reportlib.model.factory.ReportContentTestFactory
import org.scalatest.{Matchers, WordSpec}
import spray.json._

class ReportContentJsonSpec
  extends WordSpec
  with Matchers
  with ReportContentTestFactory
  with ReportJsonProtocol {

  import ReportContentTestFactory._

  "ReportContent" should {

    val emptyReportJson: JsObject = JsObject(
      "name" -> JsString(reportName),
      "reportType" -> JsString(reportType.toString),
      "tables" -> JsArray(),
      "distributions" -> JsObject()
    )
    val report = testReport
    val reportJson: JsObject = JsObject(
      "name" -> JsString(reportName),
      "reportType" -> JsString(reportType.toString),
      "tables" -> JsArray(report.tables.map(_.toJson): _*),
      "distributions" -> JsObject(report.distributions.mapValues(_.toJson))
    )

    "serialize" when {
      "empty" in {
        val report = ReportContent(reportName, reportType)
        report.toJson shouldBe emptyReportJson
      }
      "filled report" in {
        val json = report.toJson
        json shouldBe reportJson
      }
    }
    "deserialize" when {
      "empty report" in {
        emptyReportJson.convertTo[ReportContent] shouldBe ReportContent(
          reportName, reportType)
      }
      "full report" in {
        reportJson.convertTo[ReportContent] shouldBe report
      }
    }
  }
}
