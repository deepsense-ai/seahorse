/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.livy

import io.deepsense.commons.models.Id
import io.deepsense.sessionmanager.service.livy.requests.Create

trait RequestBodyBuilder {
  def createSession(workflowId: Id): Create
}
