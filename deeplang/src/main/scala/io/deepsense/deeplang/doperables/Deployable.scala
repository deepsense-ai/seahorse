/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables

import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.{DSHdfsClient, Model}

import scala.concurrent.Future

trait Deployable extends Serializable {
  def deploy(f: (Model) => Future[String]): Future[String]
}

object DeployableLoader extends Logging {
  def loadFromHdfs(hdfsClient: DSHdfsClient)(path: String): Deployable = {
    logger.debug("Trying to read path: {}", path)
    hdfsClient.readFileAsObject[Deployable](path)
  }
}
