/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.livy
import scala.concurrent.Future

import io.deepsense.commons.models.Id
import io.deepsense.sessionmanager.service.livy.responses.{Batch, BatchList}

trait Livy {
  def createSession(workflowId: Id): Future[Batch]

  def killSession(id: Int): Future[Boolean]

  def listSessions(): Future[BatchList]

  def getSession(id: Int): Future[Option[Batch]]
}
