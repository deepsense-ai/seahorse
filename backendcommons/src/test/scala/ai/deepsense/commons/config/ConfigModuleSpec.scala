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

import scala.collection.JavaConversions._

import com.google.inject.Guice
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{FunSpec, Matchers}

import ai.deepsense.commons.config.TestInjectable.Params

class ConfigModuleSpec extends FunSpec with Matchers {
  def loadConfig(fileName: String): Config = {
    System.setProperty("config.trace", "loads")
    ConfigFactory.invalidateCaches()
    ConfigFactory.load(fileName)
  }

  def getExpectedValues(config: Config): Params = {
    TestInjectable.Params(
      config.getInt("test.int"),
      config.getDouble("test.double"),
      config.getBoolean("test.boolean"),
      config.getStringList("test.stringList"),
      config.getIntList("test.intList").map(_.intValue).toSeq,
      config.getDoubleList("test.doubleList").map(_.doubleValue).toSeq,
      config.getBooleanList("test.booleanList").map(_.booleanValue).toSeq
    )
  }

  describe ("A module that injects config bindings") {
    val config = loadConfig("all-types.conf")
    val injector = Guice.createInjector(new TestModule(config))

    it ("Should successfully instantiate a class with @Named constructor parameters") {

      val expected = getExpectedValues(config)
      val instance = injector.getInstance(classOf[TestInjectable])

      instance.params equals expected
    }
  }

  describe ("A config that contains empty lists should also bind") {
    val config = loadConfig("empty-lists.conf")
    val injector = Guice.createInjector(new TestModule(config))

    it ("Should successfully instantiate a class with @Named constructor parameters") {

      val expected = getExpectedValues(config)
      val instance = injector.getInstance(classOf[TestInjectable])

      instance.params equals expected
    }
  }

}
