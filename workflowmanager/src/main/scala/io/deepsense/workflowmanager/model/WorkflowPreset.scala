/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.workflowmanager.model

import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol

import io.deepsense.commons.json.IdJsonProtocol
import io.deepsense.models.workflows.Workflow

case class WorkflowPreset(id: Workflow.Id, presetId: Long)

trait WorkflowPresetJsonProtocol extends DefaultJsonProtocol
  with IdJsonProtocol
  with SprayJsonSupport {
  implicit val workflowPresetJsonFormat = jsonFormat2(WorkflowPreset)
}

object WorkflowPresetJsonProtocol extends WorkflowPresetJsonProtocol

