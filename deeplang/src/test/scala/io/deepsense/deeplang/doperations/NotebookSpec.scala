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

package io.deepsense.deeplang.doperations

import org.mockito.Matchers.same
import org.mockito.Mockito.{verify, when}
import org.scalatest.mock.MockitoSugar

import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.{ContextualDataFrameStorage, ExecutionContext, UnitSpec}

class NotebookSpec extends UnitSpec with MockitoSugar {

  "Notebook operation" should {
    "register DataFrame" in {
      val dataFrame = mock[DataFrame]
      val dataFrameStorage = mock[ContextualDataFrameStorage]
      val executionContext = mock[ExecutionContext]
      when(executionContext.dataFrameStorage).thenReturn(dataFrameStorage)

      Notebook().execute(executionContext)(Vector(dataFrame))

      verify(dataFrameStorage).store(same(dataFrame))
    }
  }
}
