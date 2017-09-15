/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.util

import scala.util.{Failure, Success, Try}

import spray.json._

import io.deepsense.commons.utils.Logging
import io.deepsense.models.json.workflow.WorkflowWithSavedResultsJsonProtocol
import io.deepsense.models.workflows.{Workflow, WorkflowWithResults, WorkflowWithSavedResults}
import io.deepsense.workflowmanager.exceptions.{WorkflowVersionException, WorkflowVersionFormatException, WorkflowVersionNotFoundException, WorkflowVersionNotSupportedException}
import io.deepsense.workflowmanager.rest.Version

trait WorkflowVersionUtil extends WorkflowWithSavedResultsJsonProtocol {
  this: Logging =>

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
  val versionedWorkflowWithSavedResultsReader = new VersionedJsonReader[WorkflowWithSavedResults]

  def workflowWithSavedResultsOrString(
      stringJson: String): Either[String, WorkflowWithSavedResults] =
    parsedOrString(versionedWorkflowWithSavedResultsReader, stringJson)

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
          logger.warn("Could not read version in workflow's json: ", exception)
          throw WorkflowVersionNotFoundException(Version.currentVersion)
        case Success(value) => value
      }

      Try(Version(versionString)) match {
        case Failure(exception) =>
          logger.warn(s"Could not parse version in a workflow: '$versionString'", exception)
          throw WorkflowVersionFormatException(versionString)
        case Success(parsedVersion) if parsedVersion.isCompatible =>
          f(json)
        case Success(parsedVersion) if !parsedVersion.isCompatible =>
          logger.warn(s"A workflow is in a obsolete API version: '${parsedVersion.humanReadable}'")
          throw WorkflowVersionNotSupportedException(parsedVersion, Version.currentVersion)
      }
    }
  }
}
