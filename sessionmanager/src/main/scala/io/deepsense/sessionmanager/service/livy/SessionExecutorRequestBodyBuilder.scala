/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.livy

import com.google.inject.Inject
import com.google.inject.name.Named

import io.deepsense.commons.models.Id
import io.deepsense.sessionmanager.service.livy.requests.Create

/**
  * Allows to build Livy requests to run Session Executor.
  *
  * @param className Class name of the application to execute.
  * @param applicationJarPath Path to JAR with the application code.
  * @param queueHost MQ address.
  */
class SessionExecutorRequestBodyBuilder @Inject() (
  @Named("session-executor.parameters.class-name") private val className: String,
  @Named("session-executor.parameters.application-jar-path") private val applicationJarPath: String,
  @Named("session-executor.parameters.queue-host") private val queueHost: String
) extends RequestBodyBuilder {

  /**
    * Return a 'Create' request that,
    * when executed, will spawn Session Executor
    *
    * @param workflowId An identifier of a workflow that SE will operate on.
    */
  def createSession(workflowId: Id): Create = {
    Create(
      applicationJarPath,
      className,
      args = Seq(
        "--interactive-mode",
        "-m", queueHost,
        "-p", applicationJarPath,
        "-j", workflowId.toString()
      ),
      jars = Seq(applicationJarPath),
      conf = Map("spark.driver.extraClassPath" -> applicationJarPath)
    )
  }
}
