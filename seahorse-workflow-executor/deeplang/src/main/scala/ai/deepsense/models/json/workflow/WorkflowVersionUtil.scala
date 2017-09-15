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

package ai.deepsense.models.json.workflow

import scala.util.{Failure, Success, Try}

import spray.json._

import ai.deepsense.commons.utils.{Logging, Version}
import ai.deepsense.models.json.workflow.exceptions.{WorkflowVersionException, WorkflowVersionFormatException, WorkflowVersionNotFoundException, WorkflowVersionNotSupportedException}
import ai.deepsense.models.workflows.{Workflow, WorkflowWithResults, WorkflowWithVariables}

trait WorkflowVersionUtil
  extends WorkflowWithResultsJsonProtocol
  with WorkflowWithVariablesJsonProtocol {

  this: Logging =>

  def currentVersion: Version

  def extractVersion(workflow: String): Try[Version] = Try {
    val workflowJson = workflow.parseJson.asJsObject
    extractVersion(workflowJson).map(Version(_)).get
  }

  def extractVersion(json: JsValue): Try[String] = Try {
    json.asJsObject.fields("metadata")
      .asJsObject
      .fields("apiVersion")
      .convertTo[String]
  }

  val versionedWorkflowReader = new VersionedJsonReader[Workflow]
  val versionedWorkflowWithResultsReader = new VersionedJsonReader[WorkflowWithResults]
  val versionedWorkflowWithVariablesReader = new VersionedJsonReader[WorkflowWithVariables]

  def workflowOrString(stringJson: String): Either[String, Workflow] =
    parsedOrString(versionedWorkflowReader, stringJson)

  private def parsedOrString[T](reader: JsonReader[T], stringJson: String): Either[String, T] = {
    Try {
      Right(stringJson.parseJson.convertTo[T](reader))
    }.recover {
      case e: WorkflowVersionException => Left(stringJson)
    }.get
  }

  class VersionedJsonReader[T : JsonReader] extends RootJsonReader[T] {
    override def read(json: JsValue): T = {
      whenVersionCurrent(json){ _.convertTo[T] }
    }

    def whenVersionCurrent(json: JsValue)(f: (JsValue) => T): T = {
      val versionString = extractVersion(json) match {
        case Failure(exception) =>
          throw WorkflowVersionNotFoundException(currentVersion)
        case Success(value) => value
      }

      Try(Version(versionString)) match {
        case Failure(exception) =>
          throw WorkflowVersionFormatException(versionString)
        case Success(parsedVersion) if parsedVersion.compatibleWith(currentVersion) =>
          f(json)
        case Success(parsedVersion) if !parsedVersion.compatibleWith(currentVersion) =>
          throw WorkflowVersionNotSupportedException(parsedVersion, currentVersion)
      }
    }
  }
}
