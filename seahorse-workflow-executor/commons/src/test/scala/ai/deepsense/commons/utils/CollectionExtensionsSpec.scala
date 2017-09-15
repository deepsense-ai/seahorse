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

package ai.deepsense.commons.utils

import ai.deepsense.commons.StandardSpec

class CollectionExtensionsSpec extends StandardSpec {

  "Seq with RichSeq conversion" should {
    import CollectionExtensions._

    "tell that it has unique value" in {
      val s = Seq(1, 3, 2)
      s.hasUniqueValues shouldBe true
      s.hasDuplicates shouldBe false
    }
    "tell that it has duplicates" in {
      val s = Seq(1, 3, 2, 3)
      s.hasUniqueValues shouldBe false
      s.hasDuplicates shouldBe true
    }
  }
}
