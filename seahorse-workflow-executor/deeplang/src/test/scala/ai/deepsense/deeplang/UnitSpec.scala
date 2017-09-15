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

import org.scalatest._
import org.scalatest.mockito.MockitoSugar

/**
 * Base class for unit tests as advised: http://www.scalatest.org/user_guide/defining_base_classes
 */
abstract class UnitSpec
  extends WordSpec
  with Matchers
  with OptionValues
  with Inside
  with Inspectors
  with MockitoSugar
