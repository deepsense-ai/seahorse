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

package ai.deepsense.deeplang.utils

import ai.deepsense.deeplang.DeeplangIntegTestSupport

class SparkUtilsIntegSpec extends DeeplangIntegTestSupport {

  "countOccurrencesWithKeyLimit operation" should {

    "Return Some(result) if distinct value limit is not reached" in {
      val data = Seq("A", "B", "C")
      val satisfiedLimit = 3
      val result = execute(data, satisfiedLimit )
      result shouldBe defined
    }

    "Return None if distinct value limit is reached" in {
      val data = Seq("A", "B", "C")
      val unsatisfiedLimit = 2
      val result = execute(data, unsatisfiedLimit)
      result should not be defined
    }

    "Properly calculate amount of occurrences" in {
      val data = Seq("A", "A", "A", "A", "A", "B", "B")
      val result = execute(data, 3)
      result shouldBe Some(Map("A" -> 5, "B" -> 2))
    }

  }

  private def execute(data: Seq[String], limit: Int) = {
    val rdd = sparkContext.parallelize(data)
    SparkUtils.countOccurrencesWithKeyLimit(rdd, limit)
  }

}
