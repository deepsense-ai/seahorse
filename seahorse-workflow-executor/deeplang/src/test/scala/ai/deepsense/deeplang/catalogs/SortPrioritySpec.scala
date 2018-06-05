/**
 * Copyright 2018 Astraea, Inc.
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

package ai.deepsense.deeplang.catalogs

import ai.deepsense.deeplang.UnitSpec

/**
  * Test rig for catalog priority specifier.
  */
class SortPrioritySpec extends UnitSpec {
  "SortPriority" should {
    "should be ordered" in {
      SortPriority(3) should be < SortPriority(4)
      SortPriority(4) should be > SortPriority(3)
      SortPriority(4) shouldEqual SortPriority(4)
      SortPriority(4) should not equal SortPriority(3)
    }
    "generate a sequence of priorities" in {
      val cases = Seq(
        (13, 2, 100),
        (-100, 1, 31),
        (0, 99, 101),
        (Int.MinValue, 3, 10),
        (10, -5, 200)
      )
      forEvery(cases) { case (start, skip, count) =>
        val nums = SortPriority(start).inSequence(skip).take(count).map(_.value).toSeq
        assert(nums.length === count)
        assert(nums.head === start)
        assert(nums.last === start + (count - 1) * skip)
      }
    }
  }
}
