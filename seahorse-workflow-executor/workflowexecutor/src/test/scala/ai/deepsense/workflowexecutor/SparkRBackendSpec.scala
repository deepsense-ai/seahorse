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

package ai.deepsense.workflowexecutor

import org.apache.spark.api.r._
import org.scalatest.concurrent.TimeLimits
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, PrivateMethodTester, WordSpec}

import ai.deepsense.workflowexecutor.customcode.CustomCodeEntryPoint

class SparkRBackendSpec
  extends WordSpec
  with MockitoSugar
  with Matchers
  with TimeLimits
  with PrivateMethodTester {

  "Spark R Backend" should {
    "return 0 for Entry Point Id" in {
      val sparkRBackend = new SparkRBackend()
      val customCodeEntryPoint = mock[CustomCodeEntryPoint]
      sparkRBackend.start(customCodeEntryPoint)
      sparkRBackend.entryPointId shouldBe "0"
      sparkRBackend.close()
    }
  }
}
