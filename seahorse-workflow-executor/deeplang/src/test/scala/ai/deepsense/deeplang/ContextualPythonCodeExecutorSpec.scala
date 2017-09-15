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

package ai.deepsense.deeplang

import scala.concurrent.Future

import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter

import ai.deepsense.commons.models.Id

class ContextualPythonCodeExecutorSpec extends UnitSpec with BeforeAndAfter {

  val workflowId = Id.randomId
  val nodeId = Id.randomId

  val code = "some code"

  val pythonCodeExecutor = mock[CustomCodeExecutor]
  val operationExecutionDispatcher = mock[OperationExecutionDispatcher]
  val customCodeExecutionProvider = mock[CustomCodeExecutionProvider]

  var executor: ContextualCustomCodeExecutor = _

  before {
    executor = new ContextualCustomCodeExecutor(customCodeExecutionProvider, workflowId, nodeId)
    when(customCodeExecutionProvider.pythonCodeExecutor)
      .thenReturn(pythonCodeExecutor)
    when(customCodeExecutionProvider.operationExecutionDispatcher)
      .thenReturn(operationExecutionDispatcher)
  }

  "ContextualPythonCodeExecutor" should {

    "validate code" in {
      when(pythonCodeExecutor.isValid(code)).thenReturn(true)

      executor.isPythonValid(code) shouldBe true
    }

    "execute code" in {
      when(operationExecutionDispatcher.executionStarted(workflowId, nodeId))
        .thenReturn(Future.successful(Right(())))
      doNothing().when(pythonCodeExecutor).run(workflowId.toString, nodeId.toString, code)

      executor.runPython(code)
    }
  }
}
