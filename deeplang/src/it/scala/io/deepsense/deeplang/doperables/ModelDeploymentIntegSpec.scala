/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deeplang.doperables

import scala.concurrent.Future
import scala.util.Success

import org.scalatest.BeforeAndAfter

import io.deepsense.deeplang.DeeplangIntegTestSupport
import io.deepsense.deeplang.doperables.factories.TrainedRidgeRegressionTestFactory
import io.deepsense.deploymodelservice.{CreateModelResponse, Model}
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
      val id: Entity.Id = DOperableSaver
        .saveDOperableWithEntityStorageRegistration(executionContext)(
          testTrainedRidgeRegression,
          inputEntity)
        .id

      val retrieved: Deployable = DOperableLoader.load(
        executionContext.entityStorageClient)(
        DeployableLoader.loadFromHdfs(executionContext.hdfsClient))(
        executionContext.tenantId, id)
      val response = CreateModelResponse("testId")
      val toService = (model: Model) => {
        import scala.concurrent.ExecutionContext.Implicits.global
        Future(response)
      }
      val deploymentResult = retrieved.deploy(toService)
      import scala.concurrent.ExecutionContext.Implicits.global
      deploymentResult.onComplete {
        case Success(value) =>value shouldBe response
        case _ =>
      }
    }
  }


}
