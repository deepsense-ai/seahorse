/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graphexecutor

import scala.util.{Failure, Try}

import akka.actor.{Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit, TestActorRef}
import org.apache.hadoop.yarn.api.records.ApplicationId
import org.apache.hadoop.yarn.client.api.YarnClient
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest._
import org.scalatest.concurrent.Eventually

import io.deepsense.deeplang.doperations.LoadDataFrame
import io.deepsense.graph.{Node, Graph}
import io.deepsense.models.experiments.Experiment
import io.deepsense.models.messages.{Update, Launch}

class GraphExecutorClientSpec(actorSystem: ActorSystem)
  extends TestKit(actorSystem)
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll
  with Matchers {

  def this() = this(ActorSystem("GraphExecutorClientSpec"))

  "A GraphExecutorClient actor" should {
    "mark experiment as failed when spawning on cluster has failed" in {
      val gec = system.actorOf(Props(
        new MockedGraphExecutorClientActor(
          SimpleGraphExecutionIntegSuiteEntities.Name, "whatever")))

      val exp = oneNodeExperiment()
      gec ! Launch(exp)

      val Update(Some(failedExp)) = expectMsgType[Update]
      failedExp.state.status shouldBe Experiment.Status.Failed
    }
  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  def oneNodeExperiment(): Experiment = {
    val graph = Graph(
      Set(Node(
        Node.Id.randomId,
        LoadDataFrame(SimpleGraphExecutionIntegSuiteEntities.entityId.toString))))
    Experiment(
      Experiment.Id.randomId,
      SimpleGraphExecutionIntegSuiteEntities.entityTenantId,
      "name",
      graph)
  }

  class MockedGraphExecutorClientActor(
      entitystorageLabel: String,
      parentRemoteActorPath: String)
    extends GraphExecutorClientActor(entitystorageLabel, parentRemoteActorPath) {

    // The only reason to have the mocked actor class
    override def spawnOnCluster(
        experimentId: Experiment.Id,
        graphExecutionStatusesActorPath: String,
        esFactoryName: String = "default",
        applicationConfLocation: String = Constants.GraphExecutorConfigLocation):
      Try[(YarnClient, ApplicationId)] = {
      Failure(new IllegalStateException("Spawn failed"))
    }
  }
}
