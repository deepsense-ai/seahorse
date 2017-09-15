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

package io.deepsense.deeplang

import scala.concurrent.Future

import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter

import io.deepsense.commons.models.Id

class ContextualPythonCodeExecutorSpec extends UnitSpec with BeforeAndAfter {

  val workflowId = Id.randomId
  val nodeId = Id.randomId

  val code = "some code"

  val pythonCodeExecutor = mock[PythonCodeExecutor]
  val customOperationExecutor = mock[CustomOperationExecutor]

  var executor: ContextualPythonCodeExecutor = _

  before {
   executor = new ContextualPythonCodeExecutor(
     pythonCodeExecutor, customOperationExecutor, workflowId, nodeId)
  }

  "ContextualPythonCodeExecutor" should {

    "validate code" in {
      when(pythonCodeExecutor.isValid(code)).thenReturn(true)

      executor.isValid(code) shouldBe true
    }

    "execute code" in {
      when(customOperationExecutor.execute(workflowId, nodeId))
        .thenReturn(Future.successful(Right()))
      doNothing().when(pythonCodeExecutor).run(workflowId.toString, nodeId.toString, code)

      executor.run(code)
    }
  }
}
