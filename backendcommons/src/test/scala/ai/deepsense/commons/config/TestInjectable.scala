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

package ai.deepsense.commons.config

import javax.inject.{Inject, Named}

object TestInjectable {
  case class Params(
   intValue: Int,
   doubleValue: Double,
   booleanValue: Boolean,
   stringList: Seq[String],
   intList: Seq[Int],
   doubleList: Seq[Double],
   booleanList: Seq[Boolean])
}

class TestInjectable @Inject() (
  @Named("test.int")         intValue: Int,
  @Named("test.double")      doubleValue: Double,
  @Named("test.boolean")     booleanValue: Boolean,
  @Named("test.stringList")  stringList: Seq[String],
  @Named("test.intList")     intList: Seq[Int],
  @Named("test.doubleList")  doubleList: Seq[Double],
  @Named("test.booleanList") booleanList: Seq[Boolean]) {
  val params = TestInjectable.Params(
    intValue, doubleValue, booleanValue, stringList, intList, doubleList, booleanList)
}

