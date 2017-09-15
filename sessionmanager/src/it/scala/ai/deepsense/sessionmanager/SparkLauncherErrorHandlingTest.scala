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

package ai.deepsense.sessionmanager

import scalaz.Failure

import com.google.inject.Guice
import org.scalatest.Inside
import org.scalatest.concurrent.Futures
import org.scalatest.time.{Second, Seconds, Span}

import ai.deepsense.commons.StandardSpec
import ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters.SeahorseSparkLauncher
import ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark.SparkArgumentParser.UnknownOption
import ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.{SparkLauncherConfig, SparkLauncherError}

class SparkLauncherErrorHandlingTest extends StandardSpec with Futures with Inside {

  import ai.deepsense.sessionmanager.service.TestData._

  private implicit val patience = PatienceConfig(Span(10, Seconds), Span(1, Second))
  private val sparkLauncherConfig = {
    val injector = Guice.createInjector(new SessionManagerAppModule())
    injector.getInstance(classOf[SparkLauncherConfig])
  }

  "Unknown opt handling (pre spark launcher)" in {
    val clusterDetails = someClusterDetails.copy (
      params = Some("--non-existing-parameter some-value")
    )
    val creating = SeahorseSparkLauncher(Nil, sparkLauncherConfig, clusterDetails)
    inside(creating) { case Failure(error) =>
      error shouldBe an [UnknownOption]
    }
  }

  "Unknown illegal conf key in params" in {
    val clusterDetails = someClusterDetails.copy (
      params = Some("--conf not-spark.executor.extraJavaOptions=-XX:+PrintGCDetails")
    )
    val creating = SeahorseSparkLauncher(Nil, sparkLauncherConfig, clusterDetails)
    inside(creating) { case Failure(error) =>
      error shouldBe an [SparkLauncherError]
      error.getMessage should include ("'key' must start with 'spark.'")
    }

  }

}
