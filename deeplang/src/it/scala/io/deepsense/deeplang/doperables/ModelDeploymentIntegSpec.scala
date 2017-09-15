/**
 * Copyright 2015, deepsense.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.deepsense.deeplang.doperables

import java.io.File

import scala.concurrent.Future
import scala.util.Success

import org.scalatest.BeforeAndAfter

import io.deepsense.deeplang.doperables.factories.TrainedRidgeRegressionTestFactory
import io.deepsense.deeplang.{DeeplangIntegTestSupport, Model}
import io.deepsense.models.entities.{CreateEntityRequest, DataObjectReference, DataObjectReport, Entity}


class ModelDeploymentIntegSpec
  extends DeeplangIntegTestSupport
  with BeforeAndAfter
  with TrainedRidgeRegressionTestFactory {

  private val testFilePath: String = "target/tests/modelDeploymentTest"

  before {
    fileSystemClient.delete(testFilePath)
    new File(testFilePath).getParentFile.mkdirs()
  }

  private def inputEntity = CreateEntityRequest(
    executionContext.tenantId,
    "name",
    "desc",
    "dclass",
    Some(DataObjectReference(testFilePath, "{}")),
    DataObjectReport("some report"),
    saved = true)

  "Model" should {
    "be deployable" in {
      val id: Entity.Id = DOperableSaver
        .saveDOperableWithEntityStorageRegistration(executionContext)(
          testTrainedRidgeRegression,
          inputEntity)

      val retrieved: Deployable = DOperableLoader.load(
        executionContext.entityStorageClient)(
        DeployableLoader.loadFromFs(executionContext.fsClient))(
        executionContext.tenantId, id)
      val response = "testId"
      val toService = (model: Model) => {
        import scala.concurrent.ExecutionContext.Implicits.global
        Future(response)
      }
      val deploymentResult = retrieved.deploy(toService)
      import scala.concurrent.ExecutionContext.Implicits.global
      deploymentResult.onComplete {
        case Success(value) => value shouldBe response
        case _ =>
      }
    }
  }


}
