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

import com.google.inject.name.Names
import com.google.inject.{AbstractModule, TypeLiteral}
import com.typesafe.config.ConfigValueType._
import com.typesafe.config.{Config, ConfigFactory, ConfigValue}

/**
 * This module binds each property defined in the active typesafe config to
 * a ''@Named("<key>")'' annotation.  This makes any config property value
 * accessible via injection by annotating a constructor parameter with the desired
 * ''@Named("<key>")'' annotation.
 *
 * For example
 * {{{
 * class ExampleService @Inject()(@Named("example.timeout") timeout: Int) {
 *   ...
 * }
 * }}}
 * can be declared to inject timeout with the value obtained by calling
 * Config.getInt("example.timeout").
 *
 * Also binds the active configuration to the Config type so that injectable classes
 * can obtain the config object directly.
 *
 * For example
 * {{{
 * class ExampleService @Inject()(config: Config)) {
 *   timeout = config.get("example.timeout")
 *   ...
 * }
 * }}}
 * can be declared to inject config with the active Config object.
 *
 * The module also calls [[ConfigFactoryExt.enableEnvOverride]] to enable environment
 * specific configuration use the 'env' system property.
 *
 * Taken from: https://github.com/ehalpern/sandbox (MIT licence)
 * @author Eric Halpern (eric.halpern@gmail.com)
 */
class ConfigModule extends AbstractModule {
  def configure(): Unit = {
    val config = loadConfig()
    bind(classOf[Config]).toInstance(config)
    bindConfig(config)
  }

  protected[this] def loadConfig(): Config = {
    ConfigFactoryExt.enableEnvOverride()
    ConfigFactory.invalidateCaches()
    ConfigFactory.load
  }

  /**
   * Binds every entry in Config to a @Named annotation using the fully qualified
   * config key as the name.
   */
  private def bindConfig(config: Config): Unit = {
    for (entry <- config.entrySet) {
      val cv = entry.getValue
      cv.valueType match {
        case STRING | NUMBER | BOOLEAN =>
          bindPrimitive(entry.getKey, entry.getValue)
        case LIST =>
          bindList(entry.getKey, entry.getValue)
        case NULL =>
          throw new AssertionError(
            s"Did not expect NULL entry in ConfigValue.entrySet: ${cv.origin}"
          )
        case OBJECT =>
          throw new AssertionError(
            s"Did not expect OBJECT entry in ConfigValue.entrySet: ${cv.origin}"
          )
      }
    }
  }

  /**
   * Bind a primitive value (Int, Double, Boolean, String) to a
   * '@Named("<key>") annotation.
   */
  private def bindPrimitive(key: String, value: ConfigValue): Unit = {
    val unwrapped = value.unwrapped.toString
    bindConstant.annotatedWith(Names.named(key)).to(unwrapped)
  }

  /**
   * Bind a list value to '@Named("<key>") annotation.  This list contain any
   * primitive type.
   */
  private def bindList(key: String, value: ConfigValue): Unit = {
    val list = value.unwrapped.asInstanceOf[java.util.List[Any]]
    if (list.size == 0) {
      // Seq[Int|Double|Boolean] type params will only match a value bound as Seq[Any]
      bind(new TypeLiteral[Seq[Any]](){}).annotatedWith(Names.named(key)).toInstance(Seq())
      bind(new TypeLiteral[Seq[String]](){}).annotatedWith(Names.named(key)).toInstance(Seq())
    } else {
      val seq = list.get(0) match {
        case x: Integer =>
          val v = list.collect({case x: java.lang.Integer => x.intValue}).toSeq
          bind(new TypeLiteral[Seq[Any]](){}).annotatedWith(Names.named(key)).toInstance(v)
        case x: Double =>
          val v = list.collect({case x: java.lang.Double => x.doubleValue}).toSeq
          bind(new TypeLiteral[Seq[Any]](){}).annotatedWith(Names.named(key)).toInstance(v)
        case x: Boolean =>
          val v = list.collect({case x: java.lang.Boolean => x.booleanValue}).toSeq
          bind(new TypeLiteral[Seq[Any]](){}).annotatedWith(Names.named(key)).toInstance(v)
        case x: String =>
          val v = list.collect({case x: String => x}).toSeq
          bind(new TypeLiteral[Seq[String]](){}).annotatedWith(Names.named(key)).toInstance(v)
        case x =>
          throw new AssertionError("Unsupported list type " + x.getClass)
      }
    }
  }
}
