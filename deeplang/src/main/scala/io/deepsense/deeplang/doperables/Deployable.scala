/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables

import io.deepsense.deeplang.DSHdfsClient

trait Deployable extends Serializable {
  def deploy(destination: String): AnyRef
}

object DeployableLoader {
  def loadFromHdfs(hdfsClient: DSHdfsClient)(path: String): Deployable = {
    hdfsClient.readFileAsObject[Deployable](path)
  }
}
