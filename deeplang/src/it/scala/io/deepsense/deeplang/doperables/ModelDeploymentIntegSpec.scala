/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables

import org.scalatest.BeforeAndAfter

import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperables.factories.TrainedRidgeRegressionTestFactory
import io.deepsense.entitystorage.EntityStorageClientTestInMemoryImpl
import io.deepsense.models.entities.{DataObjectReference, Entity, InputEntity}

class ModelDeploymentIntegSpec
  extends DeeplangIntegTestSupport
  with BeforeAndAfter
  with TrainedRidgeRegressionTestFactory {

  private val testFilePath: String = "/tests/modelDeploymentTest"

  before {
    rawHdfsClient.delete(testFilePath, true)
  }

  private def inputEntity = InputEntity(
    executionContext.tenantId,
    "name",
    "desc",
    "dclass",
    Some(DataObjectReference(testFilePath)),
    None,
    saved = true)

  "Model" should {
    "be deployable" in {
      DOperableSaver.saveDOperableWithEntityStorageRegistration(
        executionContext)(
        testTrainedRidgeRegression,
        inputEntity)
      val id: Entity.Id = executionContext.entityStorageClient
        .asInstanceOf[EntityStorageClientTestInMemoryImpl].storage.keys.head._2
      val deploymentType = "blah"

      val retrieved: Deployable = DOperableLoader.load(
        executionContext.entityStorageClient)(
        DeployableLoader.loadFromHdfs(executionContext.hdfsClient))(
        executionContext.tenantId, id)
      val deploymentResult = retrieved.deploy(deploymentType)

      deploymentResult shouldBe deploymentType
    }
  }


}
