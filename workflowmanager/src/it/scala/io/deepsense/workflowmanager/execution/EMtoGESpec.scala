/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.execution

import scala.language.postfixOps

import io.deepsense.commons.exception.FailureCode.NodeFailure
import io.deepsense.commons.exception.FailureDescription
import io.deepsense.deeplang.doperations.LoadDataFrame
import io.deepsense.graph.{Graph, Node}
import io.deepsense.graphexecutor.SimpleGraphExecutionIntegSuiteEntities
import io.deepsense.models.messages.{Get, Launch}
import io.deepsense.models.workflows.Workflow

class EMtoGESpec extends WorkflowExecutionSupport {

  override protected def executionTimeLimitSeconds: Long = 120L

  override protected def esFactoryName: String = SimpleGraphExecutionIntegSuiteEntities.Name

  override def requiredFiles: Map[String, String] =
    Map("/SimpleDataFrame" -> SimpleGraphExecutionIntegSuiteEntities.dataFrameLocation)


  "ExperimentManager" should {
    "launch experiment and be told about COMPLETED status of experiment once all nodes COMPLETED" in {
      val experiment = oneNodeExperiment()

      testProbe.send(runningExperimentsActorRef, Launch(experiment))

      eventually {
        experimentById(experiment.id) shouldBe 'Running
        waitTillExperimentFinishes(experiment)
        experimentById(experiment.id) shouldBe 'Completed
      }
    }

    "launch experiment and be told about FAILED status of experiment after some nodes FAILED" in {
      val experiment = experimentWithFailingGraph()

      testProbe.send(runningExperimentsActorRef, Launch(experiment))

      eventually {
        waitTillExperimentFinishes(experiment)
        val state = stateOfExperiment(experiment)
        experimentById(experiment.id) shouldBe 'Failed
        val failureDescription: FailureDescription = state.error.get
        failureDescription.code shouldBe NodeFailure
        failureDescription.title shouldBe Workflow.NodeFailureMessage
        failureDescription.message shouldBe None
        failureDescription.details shouldBe Map()
      }
    }

    "get experiment by id" in {
      val experiment = oneNodeExperiment()

      testProbe.send(runningExperimentsActorRef, Launch(experiment))
      eventually {
        experimentById(experiment.id) shouldBe 'Running
      }
      testProbe.send(runningExperimentsActorRef, Get(experiment.id))
      val Some(exp) = testProbe.expectMsgType[Option[Workflow]]
      exp.isRunning shouldBe true

      // FIXME Is it really needed? Can't we leave the experiment to die itself?
      eventually {
        experimentById(experiment.id) shouldBe 'Completed
      }
    }

    "abort running experiment" is pending
  }

  def oneNodeExperiment(): Workflow = {
    val graph = Graph(
      Set(Node(
        Node.Id.randomId,
        LoadDataFrame(SimpleGraphExecutionIntegSuiteEntities.entityId.toString))))
    Workflow(
      Workflow.Id.randomId,
      SimpleGraphExecutionIntegSuiteEntities.entityTenantId,
      "name",
      graph)
  }

  def experimentWithFailingGraph(): Workflow = {
    val graph = Graph(
      Set(Node(Node.Id.randomId, LoadDataFrame("Invalid UUID for testing purposes"))))
    Workflow(
      Workflow.Id.randomId,
      "aTenantId",
      "name",
      graph)
  }
}
