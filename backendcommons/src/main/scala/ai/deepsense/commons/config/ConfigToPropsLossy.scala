/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

import java.util.Properties

import com.typesafe.config.Config

// http://stackoverflow.com/a/34982538
object ConfigToPropsLossy {
  def apply(config: Config): Properties = {
    import scala.collection.JavaConversions._

    val properties = new Properties()

    val map: Map[String, String] = config.entrySet().map { entry =>
      entry.getKey -> entry.getValue.unwrapped().toString
    } (collection.breakOut)

    properties.putAll(map)
    properties
  }
}
