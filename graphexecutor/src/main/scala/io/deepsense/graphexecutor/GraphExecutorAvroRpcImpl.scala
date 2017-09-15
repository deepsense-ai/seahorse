/**
 * Copyright (c) 2015, CodiLime, Inc.
 */
package io.deepsense.graphexecutor

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectOutputStream}
import java.nio.ByteBuffer
import io.deepsense.graph.{Status, Graph, Node}
import io.deepsense.graphexecutor.protocol.GraphExecutorAvroRpcProtocol
import io.deepsense.graphexecutor.util.ObjectInputStreamWithCustomClassLoader
import io.deepsense.models.experiments.Experiment
import io.deepsense.models.experiments.Experiment.State

/**
 * Avro RPC server implementation, allows:
 * <ul>
 * <li>sending Graph of experiment for execution,</li>
 * <li>receiving experiment execution state,</li>
 * <li>terminating running experiment</li>
 * </ul>
 *
 * Avro RPC will be used to bind communication.
 * Graph Executor will act as server, Experiment Manager will act as client.
 * Due to possibility of multiple Graph Executor instances running on every YARN cluster node,
 * dynamic port allocation is used. Port number is sent to Experiment Manager using
 * standard YARN mechanism for such cases (Application report containing RPC port number
 * and hostname of node on which Graph Executor is running).
 */
class GraphExecutorAvroRpcImpl(graphExecutor: GraphExecutor) extends GraphExecutorAvroRpcProtocol {

  /**
   * Receives Graph of experiment designated to immediate execution by Graph Executor.
   * @param graphByteBuffer Graph serialized to ByteBuffer
   * @return true on success, false if graph has been received earlier
   *         (return of false means that this is probably second method call)
   * @throws Exception on any critical problem (i.e. graph deserialization fail)
   */
  override def sendGraph(graphByteBuffer: ByteBuffer): Boolean = {
    graphExecutor.graphGuard.synchronized {
      graphExecutor.graph match {
        case Some(graph) => false
        case None =>
          val bufferIn = new ByteArrayInputStream(graphByteBuffer.array())
          var streamIn: Option[ObjectInputStreamWithCustomClassLoader] = None
          try {
            streamIn = Some(new ObjectInputStreamWithCustomClassLoader(bufferIn))
            val deserializedExperiment = streamIn.get.readObject().asInstanceOf[Experiment]
            graphExecutor.experiment = Some(deserializedExperiment)
          } finally {
            bufferIn.close()
            if (streamIn.nonEmpty) {
              streamIn.get.close()
            }
          }
          // Notify Graph Executor about received graph
          graphExecutor.graphEventBinarySemaphore.release()
          true
      }
    }
  }

  /**
   * Returns current state of experiment execution.
   * @return Graph serialized to ByteBuffer
   * @throws Exception on any critical problem (i.e. graph not received yet)
   */
  override def getExecutionState: ByteBuffer = {
    val baos = new ByteArrayOutputStream()
    var oos: Option[ObjectOutputStream] = None
    graphExecutor.graphGuard.synchronized {
      require(graphExecutor.graph.nonEmpty)
      try {
        oos = Some(new ObjectOutputStream(baos))
        oos.get.writeObject(graphExecutor.graph.get)
      } finally {
        // NOTE: Skipping close of baos.
        // According to documentation, close method of ByteArrayOutputStream has no effect,
        // furthermore its content is needed as return value.
        if (oos.nonEmpty) {
          oos.get.flush()
          oos.get.close()
        }
      }
    }
    ByteBuffer.wrap(baos.toByteArray)
  }

  /**
   * Aborts execution of graph.
   * Dequeues queued nodes, tries to stop currently processing threads.
   * @return true on success, false if graph has not been received yet
   * @throws Exception on any critical problem (i.e. node executors pool shutdown fail)
   */
  def terminateExecution(): Boolean = {
    graphExecutor.graphGuard.synchronized {
      graphExecutor.graph match {
        case Some(graph) =>
          val abortedNodes = graph.nodes
            .map(node => if (node.isQueued) node.markAborted else node)
          graphExecutor.experiment =
            Some(graphExecutor.experiment.get.copy(
              graph = Graph(abortedNodes, graph.edges),
              state = State.aborted))
          graphExecutor.executorsPool.shutdownNow()
          graphExecutor.graphEventBinarySemaphore.release()
          true
        case None => false
      }
    }
  }
}
