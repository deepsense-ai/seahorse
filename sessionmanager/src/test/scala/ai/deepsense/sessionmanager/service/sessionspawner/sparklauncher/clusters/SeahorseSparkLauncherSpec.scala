/**
 * Copyright 2016, deepsense.ai
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

package ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters

import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FunSuite, Matchers}

import ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark.SparkArgumentParser._

class SeahorseSparkLauncherSpec extends FunSuite with Matchers  with MockitoSugar {
  test("Merging Configuration Option") {
    testCases.foreach { testCase =>
      val output = testCase.inputArgs.updateConfOptions(testCase.inputKey, testCase.inputValue)
      output shouldBe testCase.output
    }
  }

  val testCases = Seq(
    TestCase("key", "value", Map("--conf" -> Set("otherKey=5"), "--con" -> Set("value=otherValue")),
      Map("--conf" -> Set("otherKey=5", "key=value"), "--con" -> Set("value=otherValue"))),
    TestCase("key", "value", Map(("--conf" -> Set("key=valueOther", "key2=value2"))),
      Map("--conf" -> Set("key=valueOther,value", "key2=value2")))
  )

  case class TestCase(
    inputKey: String,
    inputValue: String,
    inputArgs: Map[String, Set[String]],
    output: Map[String, Set[String]])

}
