/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app

import scala.concurrent.Future

import io.deepsense.experimentmanager.auth.usercontext.UserContext

trait ExperimentManagerProvider {
  def forContext(userContext: Future[UserContext]): ExperimentManager
}
