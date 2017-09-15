/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.deploymodelservice

import akka.actor.ActorRefFactory
import spray.http._

import io.deepsense.commons.StandardSpec
import io.deepsense.deeplang.Model
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
          val createdModel = responseAs[CreateModelResponse]
          noException should be thrownBy Model.Id.fromString(createdModel.id)
          repository(Model.Id.fromString(createdModel.id)) shouldBe model
        }
      }
    }

    "return correct score for Linear Regression" in {
      new {
        val modelId = Model.Id.randomId
        override val repository: ModelRepository = new ModelRepository()
      } with DeployModelService {
        repository += (modelId -> Model(false, 1.5, Seq(2.2), Seq(3.3), Seq(4.4)))
        override implicit def actorRefFactory: ActorRefFactory = system
        Post(s"/$path/$modelId", GetScoringRequest(Seq(6.0))) ~> myRoute ~> check {
          status shouldBe StatusCodes.OK
          val cr = responseAs[ScoreModelResponse]
          cr.score shouldBe 2.85 +- 0.1
        }
      }
    }

    "return correct score for Logistic Regression" in {
      new {
        val modelId = Model.Id.randomId
        override val repository: ModelRepository = new ModelRepository()
      } with DeployModelService {
        repository += (modelId -> Model(true, 0.5, Seq(2.2), Seq(3.3), Seq(5.4)))
        override implicit def actorRefFactory: ActorRefFactory = system

        Post(s"/$path/$modelId", GetScoringRequest(Seq(6.0))) ~> myRoute ~> check {
          status shouldBe StatusCodes.OK
          val cr = responseAs[ScoreModelResponse]
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

    "return 200 for version request" in {
      new {
        override val repository: ModelRepository = new ModelRepository()
      } with DeployModelService {
        override implicit def actorRefFactory: ActorRefFactory = system
        Get("/version") ~> myRoute ~> check {
          status shouldBe StatusCodes.OK
          responseAs[String] should include ("version:")
        }
      }
    }
  }
}
