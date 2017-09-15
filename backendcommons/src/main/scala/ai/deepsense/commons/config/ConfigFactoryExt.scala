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

/**
 * Convenient extensions to typesafe ConfigFactory behavior
 */
object ConfigFactoryExt {
  /**
   * Configures the typesafe config library so that it reads an environment
   * specific configuration file instead of the default application.conf.
   *
   * The prefix of the file to load is taken from the value of the 'env' system
   * property.  For example, to read production.conf rather that application.conf,
   * specify -Denv=production in the command starting the server as in:
   *
   * java -jar server.jar -Denv=production
   *
   * Taken from: https://github.com/ehalpern/sandbox (MIT licence)
   * @author Eric Halpern (eric.halpern@gmail.com)
   */
  def enableEnvOverride(): Unit = {
    val env = System.getProperty("env")
    if (env != null) {
      System.setProperty("config.resource", env + ".conf")
    }
  }
}
