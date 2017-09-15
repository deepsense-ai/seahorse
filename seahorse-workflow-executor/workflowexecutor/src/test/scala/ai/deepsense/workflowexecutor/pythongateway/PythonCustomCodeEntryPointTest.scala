/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.workflowexecutor.pythongateway

import java.util.concurrent.TimeoutException

import scala.concurrent.duration._

import org.apache.spark.SparkContext
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

import ai.deepsense.deeplang.{CustomCodeExecutor, DataFrameStorage, OperationExecutionDispatcher}
import ai.deepsense.sparkutils.SparkSQLSession
import ai.deepsense.workflowexecutor.customcode.CustomCodeEntryPoint

class PythonCustomCodeEntryPointTest extends WordSpec with MockitoSugar with Matchers {

  "PythonEntryPoint" should {
    "throw on uninitialized code executor" in {
      val entryPoint = createEntryPoint
      a[TimeoutException] shouldBe thrownBy {
        entryPoint.getCodeExecutor(100.millis)
      }
    }

    "throw on uninitialized callback server port" in {
      val entryPoint = createEntryPoint
      a[TimeoutException] shouldBe thrownBy {
        entryPoint.getPythonPort(100.millis)
      }
    }

    "return initialized code executor" in {
      val entryPoint = createEntryPoint
      val mockExecutor = mock[CustomCodeExecutor]
      entryPoint.registerCodeExecutor(mockExecutor)
      entryPoint.getCodeExecutor(100.millis) shouldBe mockExecutor
    }

    "return initialized callback server port" in {
      val entryPoint = createEntryPoint
      entryPoint.registerCallbackServerPort(4412)
      entryPoint.getPythonPort(100.millis) shouldBe 4412
    }

    "return code executor initialized while waiting on it" in {
      val entryPoint = createEntryPoint
      val mockExecutor = mock[CustomCodeExecutor]

      new Thread(new Runnable {
        override def run(): Unit = {
          Thread.sleep(1000)
          entryPoint.registerCodeExecutor(mockExecutor)
        }
      }).start()

      entryPoint.getCodeExecutor(2.seconds) shouldBe mockExecutor
    }
  }

  private def createEntryPoint: CustomCodeEntryPoint =
    new CustomCodeEntryPoint(
      mock[SparkContext],
      mock[SparkSQLSession],
      mock[DataFrameStorage],
      mock[OperationExecutionDispatcher])
}
