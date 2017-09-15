/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.config

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
