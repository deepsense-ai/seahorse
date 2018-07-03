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

package ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters

import scala.collection._
import scalaz.Scalaz._
import scalaz.Validation

import org.apache.spark.launcher.SparkLauncher

import ai.deepsense.commons.models.ClusterDetails
import ai.deepsense.sessionmanager.service.sessionspawner._
import ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.executor.SessionExecutorArgs
import ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark.SparkArgumentParser
import ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark.SparkArgumentParser._
import ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.{SparkLauncherConfig, SparkLauncherError, UnexpectedException}

object SeahorseSparkLauncher {

  def apply(
      applicationArgs: Seq[String],
      sparkLauncherConfig: SparkLauncherConfig,
      clusterConfig: ClusterDetails): Validation[SparkLauncherError, SparkLauncher] = handleUnexpectedExceptions {
    for {
      basicArgs <- SparkArgumentParser.parse(clusterConfig.params)
    } yield {
      val args = basicArgs.updateConfOptions(
        "spark.driver.extraJavaOptions", "-Dfile.encoding=UTF8 -XX:+UseG1GC", delimiter = " ")
      val sparkLauncher = clusterConfig.clusterType match {
        case ClusterType.local =>
          LocalSparkLauncher(applicationArgs, sparkLauncherConfig, clusterConfig, args)
        case ClusterType.standalone =>
          StandaloneSparkLauncher(applicationArgs, sparkLauncherConfig, clusterConfig, args)
        case ClusterType.yarn =>
          YarnSparkLauncher(applicationArgs, sparkLauncherConfig, clusterConfig, args)
        case ClusterType.mesos =>
          MesosSparkLauncher(applicationArgs, sparkLauncherConfig, clusterConfig, args)
      }

      val jars = new java.io.File(sparkLauncherConfig.sparkResourcesJarsDir)
        .listFiles.map(f => f.getAbsolutePath)
        .filter(_.endsWith(".jar"))
      val extraClassPath = s"${sparkLauncherConfig.weJarPath}:${jars.mkString(":")}"

      jars.toList.foldLeft(sparkLauncher) { (acc, jar) => acc.addJar(jar) }
        .setConf("spark.driver.extraClassPath", extraClassPath)
        .setConfOpt("spark.executor.memory", clusterConfig.executorMemory)
        .setConfOpt("spark.driver.memory", clusterConfig.driverMemory)
        .setConfOpt("spark.executor.cores", clusterConfig.executorCores.map(_.toString))
        .setConfOpt("spark.cores.max", clusterConfig.totalExecutorCores.map(_.toString))
        .setConfOpt("spark.executor.instances", clusterConfig.numExecutors.map(_.toString))
    }
  }

  private def handleUnexpectedExceptions[T, E <: SparkLauncherError](code: => Validation[E, T]) =
    try {
      code
    } catch {
      case ex: Exception => UnexpectedException(ex).failure
    }

  implicit class RichSparkLauncher(self: SparkLauncher) {
    def setConfOpt(key: String, valueOpt: Option[String]): SparkLauncher = {
      valueOpt match {
        case Some(value) => self.setConf(key, value)
        case None => self
      }
    }

    def setConfDefault(
      key: String,
      sparkArgs: SparkArgumentParser.SparkOptionsMultiMap,
      default: String): SparkLauncher = {
      sparkArgs.getConfOption(key) match {
        case None => self.setConf(key, default)
        case _ => self
      }
    }

    def setSparkArgs(args: SparkArgumentParser.SparkOptionsMultiMap): SparkLauncher =
      args.filterKeys(key =>
        !disabledKeys.contains(key)
      ).foldLeft(self) { (sparkLauncher, keyValue) =>
        val (key, value) = keyValue
        val isNoValueArgument = value == null
        if (isNoValueArgument) {
          sparkLauncher.addSparkArg(key)
        } else {
          value.foldLeft(sparkLauncher) { case (currentSparkLauncher, v) =>
            currentSparkLauncher.addSparkArg(key, v)
          }
        }
      }

    val disabledKeys = Set(
      "--class",
      "--deploy-mode",
      "--driver-class-path",
      "--driver-java-options",
      "--master",
      "--name",
      "--verbose"
    )
  }

}

