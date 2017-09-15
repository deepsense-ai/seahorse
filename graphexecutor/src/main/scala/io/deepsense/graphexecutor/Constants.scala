/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Wraps all constants. This class is an ersatz of configuration library.
 *
 * TODO: This class usage should be replaced with usage
 * of deepsense confguration library & specific configuration files.
 */

package io.deepsense.graphexecutor

object Constants {
  /** Count of threads in pool. Limits number of nodes in concurrent execution. */
  val ConcurrentGraphNodeExecutors: Int = 10

  /** Time in ms that Graph Executor will wait for graph of experiment sending. */
  val WaitingForGraphDelay = 30 * 1000

  /**
   * Time interval in ms that Graph Executor will check for nodes ready for execution.
   * Periodic checks for nodes ready for execution are performed only to increase fail-safeness.
   */
  val WaitingForNodeExecutionInterval = 1000

  /** Delay in ms between Application Master unregistration and RPC server closing. */
  val UnregisteringAndClosingRpcServerDelay = 10 * 1000

  /**
   * Time interval in ms that will be used as heartbeat interval during communication between
   * Application Manager and Resource Manager.
   */
  val AMRMClientHeartbeatInterval = 100

  /**
   * Time interval in ms that Experiment Manager mock will sleep
   * between RPC calls to Graph Execution.
   */
  val EMControlInterval = 2 * 1000

  /** Time in ms that Experiment Manager mock will wait for start of Graph Executor */
  val WaitForGraphExecutorClientInitDelay = 60 * 1000

  /** Time interval in ms that Experiment Manager mock will check for start of Graph Executor */
  val EMGraphExecutorClientInitInterval = 100

  /**
   * Directory on HDFS used for testing purposes
   */
  val TestDir = "/tests"

  /**
   * Tenant id used for testing purposes
   */
  val TestTenantId = "TestTenantId"

  /**
   * DeepSense.io deployment directory (directory on HDFS)
   */
  val DeepSenseIoDeploymentDirectory = "/deepsense"

  /**
   * Location of Graph Executor dependencies jar on cluster HDFS
   */
  val GraphExecutorDepsJarLocation =
    DeepSenseIoDeploymentDirectory + "/lib" + "/graphexecutor-deps.jar"

  /**
   * Location of Graph Executor jar on cluster HDFS
   */
  val GraphExecutorJarLocation = DeepSenseIoDeploymentDirectory + "/lib" + "/graphexecutor.jar"

  /**
   *  Configuration file's name on cluster HDFS
   */
  val GraphExecutorConfName = "graphexecutor.conf"

  /**
   * Location of configuration file on cluster HDFS
   */
  val GraphExecutorConfigLocation = DeepSenseIoDeploymentDirectory + "/etc/" + GraphExecutorConfName

  /**
   *  Configuration file's name on cluster HDFS
   */
  val EntityStorageConfName = "entitystorage-communication.conf"

  /**
   * Location of configuration file on cluster HDFS
   */
  val EntityStorageConfigLocation =
    DeepSenseIoDeploymentDirectory + "/etc/" + EntityStorageConfName

  /**
   *  Configuration file's name on cluster HDFS
   */
  val Log4jPropertiesName = "log4j.properties"

  /**
   * Location of configuration file on cluster HDFS
   */
  val Log4jPropertiesLocation = DeepSenseIoDeploymentDirectory + "/etc/" + Log4jPropertiesName
}
