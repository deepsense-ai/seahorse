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

package ai.deepsense.deeplang.catalogs.doperations

import ai.deepsense.deeplang.DOperationCategories.UserDefined
import ai.deepsense.deeplang.{CatalogRecorder, UnitSpec}

class DOperationRegistrationSpec extends UnitSpec {

  "DOperationsCatalog" should {
    val catalogs = CatalogRecorder.resourcesCatalogRecorder.catalogs
    val operations = catalogs.operations
    "successfully register and create all DOperations" in {
      operations.operations.keys.foreach(id => operations.createDOperation(id))
    }
    "report assigned categories" in {
      val delta = catalogs.categories.categories.diff(operations.categories.categories)
      delta match {
        case Seq(cat) if cat == UserDefined => ()
        case _ => fail("Expected UserDefined as the single difference. Had " + delta)
      }
    }
  }
}
