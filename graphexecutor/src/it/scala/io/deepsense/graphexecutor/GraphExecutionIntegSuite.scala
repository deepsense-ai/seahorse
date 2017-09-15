/**
 * Copyright (c) 2015, CodiLime Inc.
 */
package io.deepsense.graphexecutor

import scala.concurrent.Future

import akka.actor.Actor
import com.typesafe.scalalogging.LazyLogging
import org.scalatest.{BeforeAndAfter, Matchers}

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.deeplang.DOperation
import io.deepsense.graph._
import io.deepsense.models.experiments.Experiment

/**
 * NOTE: You can observe results of this test suite by running command on HDFS cluster:
 * hadoop fs -ls /deepsense/TestTenantId/dataframe
 */
abstract class GraphExecutionIntegSuite
  extends HdfsIntegTestSupport
  with Matchers
  with BeforeAndAfter {

  val created = DateTimeConverter.now
  val updated = created.plusHours(1)

  experimentName should {
    "run on external YARN cluster" in {
    }
  }

  protected def experiment = Experiment(
    Experiment.Id.randomId,
    tenantId,
    experimentName,
    Graph(nodes.toSet, edges.toSet),
    created,
    updated)

  protected def node(operation: DOperation): Node = Node(Node.Id.randomId, operation)

  protected def nodes: Seq[Node]

  protected def edges: Seq[Edge]

  protected def experimentName: String

  protected def esFactoryName: String

  protected def tenantId: String
}
