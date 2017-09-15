/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 */

package io.deepsense.modeldeploying

import akka.actor.Actor
import spray.routing._

import io.deepsense.modeldeploying.ModelDeploingJsonProtocol._

// We don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class ModelDeployingServiceActor extends Actor with ModelDeployingService {

  // The HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // This actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)

  override val repository: ModelRepository = new ModelRepository()
}

// This trait defines our service behavior independently from the service actor
trait ModelDeployingService extends HttpService {

  val path = "regression"
  val repository: ModelRepository

  val myRoute = pathPrefix(path) {
    path(JavaUUID) { idParameter =>
      get {
        entity(as[GetScoringRequest]) { getScoringRequest =>
          val model = repository.get(idParameter)
          val score = model.score(getScoringRequest)
          complete(ScoreResult(score))
        }
      }
    } ~ post {
      entity(as[Model]) { model =>
        complete(CreateResult(repository.put(model).toString))
      }
    }
  }
}

case class CreateResult(id: String)

case class GetScoringRequest(features: Seq[Double])

case class ScoreResult(score: Double)
