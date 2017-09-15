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

import org.scalatest.BeforeAndAfter
import org.scalatest.mockito.MockitoSugar
import ai.deepsense.commons.StandardSpec
import ai.deepsense.commons.models.Id
import ai.deepsense.deeplang.OperationExecutionDispatcher

class OperationExecutionDispatcherSpec
    extends StandardSpec
    with MockitoSugar
    with BeforeAndAfter {

  val workflowId = Id.randomId
  val nodeId = Id.randomId

  var dispatcher: OperationExecutionDispatcher = _

  before {
    dispatcher = new OperationExecutionDispatcher
  }

  "OperationExecutionDispatcher" should {

    "execute operation and finish" when {

      "notified of success with proper workflow and node id" in {
        val future = dispatcher.executionStarted(workflowId, nodeId)
        future.isCompleted shouldBe false

        dispatcher.executionEnded(workflowId, nodeId, Right(()))
        future.isCompleted shouldBe true
      }

      "notified of failure with proper workflow and node id" in {
        val future = dispatcher.executionStarted(workflowId, nodeId)
        future.isCompleted shouldBe false

        dispatcher.executionEnded(workflowId, nodeId, Left("A stacktrace"))
        future.isCompleted shouldBe true
        future.value.get.get shouldBe Left("A stacktrace")
      }
    }

    "throw an exception" when {

      "multiple executions of the same node are started" in {
        dispatcher.executionStarted(workflowId, nodeId)

        an[IllegalArgumentException] shouldBe thrownBy {
          dispatcher.executionStarted(workflowId, nodeId)
        }
      }

      "notified with non-existing workflow id" in {
        val future = dispatcher.executionStarted(workflowId, nodeId)
        future.isCompleted shouldBe false

        an[IllegalArgumentException] shouldBe thrownBy {
          dispatcher.executionEnded(Id.randomId, nodeId, Right(()))
        }
      }

      "notified with non-existing node id" in {
        val future = dispatcher.executionStarted(workflowId, nodeId)
        future.isCompleted shouldBe false

        an[IllegalArgumentException] shouldBe thrownBy {
          dispatcher.executionEnded(workflowId, Id.randomId, Right(()))
        }
      }
    }
  }
}
