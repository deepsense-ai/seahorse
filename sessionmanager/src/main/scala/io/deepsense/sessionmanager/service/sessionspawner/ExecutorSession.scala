/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner

import java.time.Instant

import org.apache.spark.launcher.SparkAppHandle

import io.deepsense.commons.models.ClusterDetails
import io.deepsense.sessionmanager.service.{Session, Status}
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.outputintercepting.OutputInterceptorHandle

case class ExecutorSession(
    sessionConfig: SessionConfig,
    clusterDetails: ClusterDetails,
    private val sparkAppHandleOpt: Option[SparkAppHandle],
    state: StateInferencer,
    private val outputInterceptorHandle: OutputInterceptorHandle) {

  def sessionForApi(): Session = {
    val status = sparkAppHandleOpt match {
      case None => Status.Error // no working spark process at all
      case Some(sparkAppHandle) => state.statusForApi(Instant.now(), sparkAppHandle.getState)
    }
    Session(
      sessionConfig.workflowId,
      status,
      clusterDetails
    )
  }

  def handleHeartbeat() = this.copy(
    state = state.handleHeartbeat(Instant.now())
  )

  def kill(): Unit = {
    sparkAppHandleOpt.foreach(_.kill())
    outputInterceptorHandle.close()
  }

}
