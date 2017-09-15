package io.deepsense.experimentmanager.rest.actions

import java.util.UUID

import scala.concurrent.Future

import akka.actor.ActorSystem
import spray.client.pipelining._
import spray.httpx.SprayJsonSupport

import io.deepsense.commons.auth.usercontext.UserContext
import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.doperables.{DOperableLoader, Deployable, DeployableLoader}
import io.deepsense.deploymodelservice.DeployModelJsonProtocol._
import io.deepsense.deploymodelservice.{CreateResult, Model}


class DeployModel {

  def deploy(id: UUID, uc: UserContext, ec: ExecutionContext): Future[CreateResult] = {
    val retrieved: Deployable = DOperableLoader.load(
      ec.entityStorageClient)(
        DeployableLoader.loadFromHdfs(ec.hdfsClient))(
        uc.tenantId, id)
    val toService = (model: Model) => {
      implicit val system = ActorSystem()
      import SprayJsonSupport._
      import system.dispatcher
      val pipeline = sendReceive ~> unmarshal[CreateResult]
      val response: Future[CreateResult] = pipeline {
        Post("http://localhost:8082/regression", model)
      }
      response
    }

    retrieved.deploy(toService)
  }
}
