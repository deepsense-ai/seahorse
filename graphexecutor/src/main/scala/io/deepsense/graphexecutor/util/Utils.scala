/**
 * Copyright (c) 2015, CodiLime Inc.
 */

/**
 * Utilities for submitting YARN applications.
 */
package io.deepsense.graphexecutor.util

import java.io.File

import scala.collection.JavaConverters._
import scala.collection.mutable.Map

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.yarn.api.ApplicationConstants
import org.apache.hadoop.yarn.api.ApplicationConstants.Environment
import org.apache.hadoop.yarn.api.records.{LocalResource, LocalResourceType, LocalResourceVisibility}
import org.apache.hadoop.yarn.conf.YarnConfiguration
import org.apache.hadoop.yarn.util.{Apps, ConverterUtils, Records}

object Utils {
  private val logDir = ApplicationConstants.LOG_DIR_EXPANSION_VAR
  /** Shell output redirection string */
  val logRedirection = s" 1>> $logDir/stdout 2>> $logDir/stderr; "

  /**
   * Configures LocalResource using file on cluster file system.
   * @param resourcePath path to resource
   * @param conf
   * @return configuref LocalResource
   */
  def getConfiguredLocalResource(resourcePath: Path)
                        (implicit conf: Configuration): LocalResource = {
    println(s"Configuration: $conf")
    val resource = Records.newRecord(classOf[LocalResource])
    val jarPath = FileSystem.get(conf).makeQualified(resourcePath)
    val jarStat = FileSystem.get(conf).getFileStatus(jarPath)
    resource.setResource(ConverterUtils.getYarnUrlFromPath(jarPath))
    resource.setSize(jarStat.getLen)
    resource.setTimestamp(jarStat.getModificationTime)
    resource.setType(LocalResourceType.FILE)
    resource.setVisibility(LocalResourceVisibility.PUBLIC)
    resource
  }

  /**
   * Sets CLASSPATH environment variable to allow running java/scala applications on YARN container.
   * @param conf
   * @return mutable map of environment variables
   */
  def getConfiguredEnvironmentVariables(implicit conf: YarnConfiguration): Map[String, String] = {
    val env = collection.mutable.Map[String, String]()
    val classPath =
      conf.getStrings(
        YarnConfiguration.YARN_APPLICATION_CLASSPATH,
        YarnConfiguration.DEFAULT_YARN_APPLICATION_CLASSPATH: _*)
    for (c <- classPath){
      Apps.addToEnvironment(env.asJava, Environment.CLASSPATH.name(), c.trim(), File.pathSeparator)
    }
    Apps.addToEnvironment(
      env.asJava,
      Environment.CLASSPATH.name(),
      Environment.PWD.$() + File.separator + "*",
      File.pathSeparator)
    env
  }
}
