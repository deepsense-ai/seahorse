/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 */

package io.deepsense.modeldeploying

import java.util.UUID

import scala.collection.mutable

import akka.actor.ActorRefFactory
import spray.http._

import io.deepsense.commons.StandardSpec
import io.deepsense.modeldeploying.ModelDeploingJsonProtocol._

class ModelDeployingServiceSpec extends StandardSpec {

  "ModelDeployingService" should {
    "return id on post model request" in {
      new {
        val repository: ModelRepository = new ModelRepository()
      } with ModelDeployingService {
        override implicit def actorRefFactory: ActorRefFactory = system

        val model = Model(false, 2.5, Seq(1.2), Seq(1.3), Seq(1.4))
        Post(s"/$path", model) ~> myRoute ~> check {
          status should be(StatusCodes.OK)
          val cr = responseAs[CreateResult]
          noException should be thrownBy UUID.fromString(cr.id)
          repository.get(UUID.fromString(cr.id)) should be(model)
        }
      }
    }

    "return correct score for Linear Regression" in {
      new {
        val uuid = UUID.randomUUID()
        val map = mutable.Map((uuid -> Model(false, 1.5, Seq(2.2), Seq(3.3), Seq(4.4))))
        val repository: ModelRepository = new ModelRepository(map)
      } with ModelDeployingService {
        override implicit def actorRefFactory: ActorRefFactory = system

        Get(s"/$path/$uuid", GetScoringRequest(Seq(6.0))) ~> myRoute ~> check {
          status should be(StatusCodes.OK)
          val cr = responseAs[ScoreResult]
          cr.score should be(2.85 +- 0.1)
        }
      }
    }

    "return correct score for Logistic Regression" in {
      new {
        val uuid = UUID.randomUUID()
        val map = mutable.Map((uuid -> Model(true, 0.5, Seq(2.2), Seq(3.3), Seq(5.4))))
        val repository: ModelRepository = new ModelRepository(map)
      } with ModelDeployingService {
        override implicit def actorRefFactory: ActorRefFactory = system

        Get(s"/$path/$uuid", GetScoringRequest(Seq(6.0))) ~> myRoute ~> check {
          status should be(StatusCodes.OK)
          val cr = responseAs[ScoreResult]
          cr.score should be(0.83 +- 0.01)
        }
      }
    }

    "leave GET requests to other paths unhandled" in {
      new {
        val repository: ModelRepository = new ModelRepository()
      } with ModelDeployingService {
        override implicit def actorRefFactory: ActorRefFactory = system

        Get("/kermit") ~> myRoute ~> check {
          handled should be(false)
        }
      }
    }
  }
}
