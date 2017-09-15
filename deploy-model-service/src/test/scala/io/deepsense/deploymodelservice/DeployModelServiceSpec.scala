/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deploymodelservice

import java.util.UUID

import scala.collection.mutable

import akka.actor.ActorRefFactory
import spray.http._

import io.deepsense.commons.StandardSpec
import io.deepsense.deploymodelservice.DeployModelJsonProtocol._

class DeployModelServiceSpec extends StandardSpec {

  "ModelDeployingService" should {
    "return id on post model request" in {
      new {
        override val repository: ModelRepository = new ModelRepository()
      } with DeployModelService {
        override implicit def actorRefFactory: ActorRefFactory = system

        val model = Model(false, 2.5, Seq(1.2), Seq(1.3), Seq(1.4))
        Post(s"/$path", model) ~> myRoute ~> check {
          status shouldBe StatusCodes.OK
          val cr = responseAs[CreateResult]
          noException should be thrownBy UUID.fromString(cr.id)
          repository(UUID.fromString(cr.id)) shouldBe model
        }
      }
    }

    "return correct score for Linear Regression" in {
      new {
        val uuid = UUID.randomUUID()
        override val repository: ModelRepository = new ModelRepository()
      } with DeployModelService {
        repository += (uuid -> Model(false, 1.5, Seq(2.2), Seq(3.3), Seq(4.4)))
        override implicit def actorRefFactory: ActorRefFactory = system
        Post(s"/$path/$uuid", GetScoringRequest(Seq(6.0))) ~> myRoute ~> check {
          status shouldBe StatusCodes.OK
          val cr = responseAs[ScoreResult]
          cr.score shouldBe 2.85 +- 0.1
        }
      }
    }

    "return correct score for Logistic Regression" in {
      new {
        val uuid = UUID.randomUUID()
        override val repository: ModelRepository = new ModelRepository()
      } with DeployModelService {
        repository += (uuid -> Model(true, 0.5, Seq(2.2), Seq(3.3), Seq(5.4)))
        override implicit def actorRefFactory: ActorRefFactory = system

        Post(s"/$path/$uuid", GetScoringRequest(Seq(6.0))) ~> myRoute ~> check {
          status shouldBe StatusCodes.OK
          val cr = responseAs[ScoreResult]
          cr.score shouldBe 0.83 +- 0.01
        }
      }
    }

    "leave POST requests to other paths unhandled" in {
      new {
        override val repository: ModelRepository = new ModelRepository()
      } with DeployModelService {
        override implicit def actorRefFactory: ActorRefFactory = system

        Post("/kermit") ~> myRoute ~> check {
          handled shouldBe false
        }
      }
    }
  }
}
