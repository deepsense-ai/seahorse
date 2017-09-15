/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables

import scala.concurrent.Future

import io.deepsense.deeplang.DSHdfsClient
import io.deepsense.deploymodelservice.{Model, CreateResult}

trait Deployable extends Serializable {
  def deploy(f:(Model)=>Future[CreateResult]): Future[CreateResult]
}

object DeployableLoader {
  def loadFromHdfs(hdfsClient: DSHdfsClient)(path: String): Deployable = {
    hdfsClient.readFileAsObject[Deployable](path)
  }
}
