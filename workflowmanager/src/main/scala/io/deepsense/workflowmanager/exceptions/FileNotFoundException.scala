/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.exceptions

import io.deepsense.commons.exception.FailureCode
import io.deepsense.models.entities.Entity

case class FileNotFoundException(entityId: Entity.Id)
  extends WorkflowManagerException(
    FailureCode.EntityNotFound,
    "File not found",
    s"File with id $entityId not found") {
  override protected def additionalDetails: Map[String, String] =
    Map("entityId" -> entityId.toString)
}
