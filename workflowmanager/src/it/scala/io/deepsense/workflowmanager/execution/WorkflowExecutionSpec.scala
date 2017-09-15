/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.execution

import scala.language.postfixOps

import io.deepsense.graph.{Edge, Graph, Node}
import io.deepsense.models.workflows.Workflow
import io.deepsense.models.messages.Launch

abstract class WorkflowExecutionSpec extends WorkflowExecutionSupport {

  protected def executionTimeLimitSeconds: Long

  protected def nodes: Seq[Node]

  protected def edges: Seq[Edge]

  protected def experimentName: String

  protected def esFactoryName: String

  protected def tenantId: String


  "ExperimentManager" should {
    s"execute experiment $experimentName" in {
      val experiment = Workflow(
        Workflow.Id.randomId,
        tenantId,
        experimentName,
        Graph(nodes.toSet, edges.toSet))

      testProbe.send(runningExperimentsActorRef, Launch(experiment))

      eventually {
        experimentById(experiment.id) shouldBe 'Running
        waitTillExperimentFinishes(experiment)
        experimentById(experiment.id) shouldBe 'Completed
      }
    }
  }
}
