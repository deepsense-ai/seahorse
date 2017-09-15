/**
 * Copyright 2015, deepsense.io
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

package io.deepsense.workflowexecutor.pythongateway

import java.util.concurrent.TimeoutException

import scala.concurrent.duration._

import org.apache.spark.SparkContext
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.mock.MockitoSugar

import io.deepsense.deeplang.{PythonCodeExecutor, CustomOperationDataFrameStorage, ReadOnlyDataFrameStorage}
import io.deepsense.workflowexecutor.pythongateway.PythonEntryPoint.PythonEntryPointConfig

class PythonEntryPointTest extends WordSpec with MockitoSugar with Matchers {

  "PythonEntryPoint" should {
    "throw on uninitialized code executor" in {
      val entryPoint = createEntryPoint()
      a[TimeoutException] shouldBe thrownBy {
        entryPoint.getCodeExecutor
      }
    }

    "throw on uninitialized callback server port" in {
      val entryPoint = createEntryPoint()
      a[TimeoutException] shouldBe thrownBy {
        entryPoint.getPythonPort
      }
    }

    "return initialized code executor" in {
      val entryPoint = createEntryPoint()
      val mockExecutor = mock[PythonCodeExecutor]
      entryPoint.registerCodeExecutor(mockExecutor)
      entryPoint.getCodeExecutor shouldBe mockExecutor
    }

    "return initialized callback server port" in {
      val entryPoint = createEntryPoint()
      entryPoint.registerCallbackServerPort(4412)
      entryPoint.getPythonPort shouldBe 4412
    }

    "return code executor initialized while waiting on it" in {
      val entryPoint = createEntryPoint(2.seconds)
      val mockExecutor = mock[PythonCodeExecutor]

      new Thread(new Runnable {
        override def run(): Unit = {
          Thread.sleep(1000)
          entryPoint.registerCodeExecutor(mockExecutor)
        }
      }).start()

      entryPoint.getCodeExecutor shouldBe mockExecutor
    }
  }

  private def createEntryPoint(timeout: Duration = 100.millis): PythonEntryPoint =
    new PythonEntryPoint(
      PythonEntryPointConfig(timeout),
      mock[SparkContext],
      mock[ReadOnlyDataFrameStorage],
      mock[CustomOperationDataFrameStorage],
      mock[OperationExecutionDispatcher])
}
