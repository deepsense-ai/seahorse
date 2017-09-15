/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.exceptions

import io.deepsense.commons.exception.FailureCode
import io.deepsense.models.entities.Entity

case class FileNotFoundException(entityId: Entity.Id)
  extends ExperimentManagerException(
    FailureCode.EntityNotFound,
    "File not found",
    s"File with id $entityId not found") {
  override protected def additionalDetails: Map[String, String] =
    Map("entityId" -> entityId.toString)
}
