/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deploymodelservice

import scala.collection.mutable

import akka.actor.{Actor, ActorContext}
import buildinfo.BuildInfo
import spray.http.HttpHeaders
import spray.routing._

import io.deepsense.commons.utils.Logging
import io.deepsense.deploymodelservice.DeployModelJsonProtocol._

class DeployModelServiceActor extends Actor with DeployModelService {

  override def actorRefFactory: ActorContext = context

  override def receive: Actor.Receive = runRoute(myRoute)

  override val repository: ModelRepository = new ModelRepository()
}

trait DeployModelService extends HttpService with Logging {

  val path = "regression"
  val repository: ModelRepository

  val AccessControlAllowAll = HttpHeaders.RawHeader(
    "Access-Control-Allow-Origin", "*"
  )
  val AccessControlAllowHeadersAll = HttpHeaders.RawHeader(
    "Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept"
  )
  val myRoute = pathPrefix(path) {
    path(JavaUUID) { id =>
      options {
        respondWithHeaders(AccessControlAllowAll, AccessControlAllowHeadersAll) {
          complete("")
        }
      } ~
      post {
        respondWithHeaders(AccessControlAllowAll) {
          entity(as[GetScoringRequest]) { request =>
            logger.debug("Get Scoring: {}", id)
            val model = repository(id)
            val score = model.score(request)
            complete(ScoreModelResponse(score))
          }
        }
      }
    } ~
    post {
      entity(as[Model]) { model =>
        logger.debug("Post Model: {}", model)
        val modelId = Model.Id.randomId
        repository.put(modelId, model)
        logger.debug("Inserted model: {}", model)
        val result = CreateModelResponse(modelId.toString)
        logger.debug("CreatedModel: {}", result)
        complete(result)
      }
    }
  } ~
  path("version") {
    get {
      complete(s"name: deploy-model-service, version: ${BuildInfo.version}, " +
        s"scalaVersion: ${BuildInfo.scalaVersion}, sbtVersion: ${BuildInfo.sbtVersion}, " +
        s"gitCommitId: ${BuildInfo.gitCommitId}")
    }
  }
}

case class ModelRepository() extends mutable.HashMap[Model.Id, Model]

case class CreateModelResponse(id: String)

case class GetScoringRequest(features: Seq[Double])

case class ScoreModelResponse(score: Double)
