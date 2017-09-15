/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager

import scala.concurrent.Future

import io.deepsense.commons.auth.usercontext.UserContext

trait WorkflowManagerProvider {
  def forContext(userContext: Future[UserContext]): WorkflowManager
}
