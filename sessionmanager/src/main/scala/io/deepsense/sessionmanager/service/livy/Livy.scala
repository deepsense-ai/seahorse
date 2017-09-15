/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.livy
import scala.concurrent.Future

import io.deepsense.commons.models.Id
import io.deepsense.sessionmanager.service.livy.responses.Batch

trait Livy {
  def createSession(workflowId: Id): Future[Batch]
}
