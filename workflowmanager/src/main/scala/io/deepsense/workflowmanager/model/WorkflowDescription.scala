/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.workflowmanager.model

import spray.json.DefaultJsonProtocol

case class WorkflowDescription(name: String, description: String)


trait WorkflowDescriptionJsonProtocol extends DefaultJsonProtocol {
  implicit val workflowDescriptionJsonFormat = jsonFormat2(WorkflowDescription)
}

object WorkflowDescriptionJsonProtocol extends WorkflowDescriptionJsonProtocol
