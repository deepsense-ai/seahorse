/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables

import scala.concurrent.Future

import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.DSHdfsClient
import io.deepsense.deploymodelservice.{Model, CreateModelResponse}

trait Deployable extends Serializable {
  def deploy(f: (Model) => Future[CreateModelResponse]): Future[CreateModelResponse]
}

object DeployableLoader extends Logging {
  def loadFromHdfs(hdfsClient: DSHdfsClient)(path: String): Deployable = {
    logger.debug("Trying to read path: {}", path)
    hdfsClient.readFileAsObject[Deployable](path)
  }
}
