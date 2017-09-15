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

package ai.deepsense.sessionmanager.service

import java.time.Instant

import org.apache.spark.launcher.SparkAppHandle
import org.apache.spark.launcher.SparkAppHandle.State
import org.scalatest.{FreeSpec, Matchers}

import ai.deepsense.sessionmanager.service.sessionspawner.{StateInferencer, StateInferencerFactory}

class SessionStateInferencerTest extends FreeSpec with Matchers {

  info(
    """The session status depends on two things:
      |- state of the underlying Apache Spark application process
      |- heartbeats send by the Apache Spark application process
    """.stripMargin)

  """The Apache Spark application might be in Running state, but it does not mean
    | that the Session is ready to work.""".stripMargin - {

    val runningApacheSparkApplication = newSessionLife()
      .updateSparkAppState(State.CONNECTED)
      .updateSparkAppState(State.SUBMITTED)
      .updateSparkAppState(State.RUNNING)

    "We can only be sure everything is correctly set up " +
      "after receiving a heartbeat from the Session." - {

      "And until the first heartbeat Session is marked as `creating`" in {
        runningApacheSparkApplication.apiStatus shouldBe Status.Creating
      }

      val afterFirstHeartbeat = runningApacheSparkApplication
        .advanceTimeBySeconds(5)
        .heartbeat()

      "After the first heartbeat Session is marked as `running`!" in {
        afterFirstHeartbeat.apiStatus shouldBe Status.Running
      }

      """Even though the Session is running, heartbeats are still used for healthchecking.
        |If Session fails to send heartbeats with some frequency. """.stripMargin - {

        val delayedHeartbeat = afterFirstHeartbeat.advanceTimeBySeconds(10)

        "It is marked as error" in {
          delayedHeartbeat.apiStatus shouldBe Status.Error
        }

        "But if Executor somehow start heartbeating again we mark it as running" in {
          delayedHeartbeat.heartbeat().apiStatus shouldBe Status.Running
        }

      }

    }

    "If we don't receive the first heartbeat in time" - {
      val noHeartbeat = runningApacheSparkApplication.advanceTimeBySeconds(200)

      "We assume the Executor is broken" in {
        noHeartbeat.apiStatus shouldBe Status.Error
      }

      """If the user did not kill the Executor yet and somehow the Session starts heartbeating
        |after a delay, there is no harm in marking it as running again!""".stripMargin in {
        noHeartbeat.heartbeat().apiStatus shouldBe Status.Running
      }
    }

  }

  """The underlying Apache Spark application runs in an infinite loop.
    |It means that if the underlying Apache Spark application has stopped
    |something went wrong""".stripMargin in {
    val happyPath = newSessionLife()
      .updateSparkAppState(State.CONNECTED)
      .updateSparkAppState(State.SUBMITTED)
      .updateSparkAppState(State.RUNNING)
      .advanceTimeBySeconds(3)
      .heartbeat()

    val unexpectedEnding = happyPath.updateSparkAppState(State.FAILED)

    unexpectedEnding.apiStatus shouldEqual Status.Error
  }

  case class SessionLife(
    currentTime: Instant,
    currentSparkState: SparkAppHandle.State,
    executorSession: StateInferencer
  ) {
    def apiStatus = executorSession.statusForApi(currentTime, currentSparkState)

    def advanceTimeBySeconds(seconds: Int): SessionLife = this.copy(
      currentTime = currentTime.plusSeconds(seconds)
    )
    def heartbeat() = this.copy(
      executorSession = executorSession.handleHeartbeat(currentTime)
    )
    def updateSparkAppState(state: State) = this.copy(
      currentSparkState = state
    )
  }

  def newSessionLife(): SessionLife = {
    val factory = new StateInferencerFactory(60, 10)
    val startTime = Instant.now()
    val executorSession = factory.newInferencer(startTime)
    val initialSessionLife = SessionLife(startTime, State.UNKNOWN, executorSession)
    initialSessionLife
  }
}
