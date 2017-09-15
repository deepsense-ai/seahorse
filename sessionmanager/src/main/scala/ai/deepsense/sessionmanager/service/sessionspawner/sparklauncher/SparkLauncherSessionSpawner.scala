/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher

import java.time.Instant

import scalaz.Scalaz._
import scalaz.Validation.FlatMap._
import scalaz._

import com.google.inject.Inject

import ai.deepsense.commons.models.ClusterDetails
import ai.deepsense.commons.utils.Logging
import ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters.SeahorseSparkLauncher
import ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.executor.SessionExecutorArgs
import ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.outputintercepting.OutputInterceptorFactory
import ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark.LoggingSparkAppListener
import ai.deepsense.sessionmanager.service.sessionspawner.{ExecutorSession, SessionConfig, SessionSpawner, StateInferencerFactory}

class SparkLauncherSessionSpawner @Inject()(
  private val sparkLauncherConfig: SparkLauncherConfig,
  private val outputInterceptorFactory: OutputInterceptorFactory,
  private val stateInferencerFactory: StateInferencerFactory
) extends SessionSpawner with Logging {

  override def createSession(
      sessionConfig: SessionConfig,
      clusterDetails: ClusterDetails): ExecutorSession = {
    logger.info(s"Creating session for workflow ${sessionConfig.workflowId}")

    val interceptorHandle = outputInterceptorFactory.prepareInterceptorWritingToFiles(
      clusterDetails
    )

    val applicationArgs = SessionExecutorArgs(sessionConfig, sparkLauncherConfig)
    val startedSession = for {
      launcher <- SeahorseSparkLauncher(applicationArgs, sparkLauncherConfig, clusterDetails)
      listener = new LoggingSparkAppListener()
      handle <- handleUnexpectedExceptions {
        interceptorHandle.attachTo(launcher)
        launcher.startApplication(listener)
      }
      stateInferencer = stateInferencerFactory.newInferencer(Instant.now())
      executorSession = ExecutorSession(
        sessionConfig, clusterDetails, Some(handle), stateInferencer, interceptorHandle
      )
    } yield executorSession

    startedSession.fold(error => {
      interceptorHandle.writeOutput(error.getMessage)
      val stateInferencer = stateInferencerFactory.newInferencer(Instant.now())
      ExecutorSession(sessionConfig, clusterDetails, None, stateInferencer, interceptorHandle)
    }, identity)
  }

  private def handleUnexpectedExceptions[T, E <: SparkLauncherError]
      (code: => T): Validation[UnexpectedException, T] =
    try {
      code.success
    } catch {
      case ex: Exception => UnexpectedException(ex).failure
    }

}
