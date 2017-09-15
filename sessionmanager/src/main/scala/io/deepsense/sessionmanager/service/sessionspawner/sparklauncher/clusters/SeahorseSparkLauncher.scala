/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters

import scala.collection._
import scalaz.Scalaz._
import scalaz.{Failure, Success, Validation}
import scalaz.Validation.FlatMap._

import org.apache.spark.launcher.SparkLauncher

import io.deepsense.commons.models.ClusterDetails
import io.deepsense.sessionmanager.service.sessionspawner._
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark.SparkArgumentParser
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark.SparkArgumentParser._
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.{SparkLauncherConfig, SparkLauncherError, UnexpectedException}

object SeahorseSparkLauncher {

  def apply(sessionConfig: SessionConfig,
            sparkLauncherConfig: SparkLauncherConfig,
            clusterConfig: ClusterDetails) = handleUnexpectedExceptions {

    for {
      basicArgs <- SparkArgumentParser.parse(clusterConfig.params)
    } yield {
      val args = basicArgs.updateConfOptions(
        "spark.driver.extraJavaOptions", "-Dfile.encoding=UTF8", delimiter = " ")
      val sparkLauncher = clusterConfig.clusterType match {
        case ClusterType.local =>
          LocalSparkLauncher(sessionConfig, sparkLauncherConfig, clusterConfig, args)
        case ClusterType.standalone =>
          StandaloneSparkLauncher(sessionConfig, sparkLauncherConfig, clusterConfig, args)
        case ClusterType.yarn =>
          YarnSparkLauncher(sessionConfig, sparkLauncherConfig, clusterConfig, args)
        case ClusterType.mesos =>
          MesosSparkLauncher(sessionConfig, sparkLauncherConfig, clusterConfig, args)
      }
      sparkLauncher.setConfOpt("spark.executor.memory", clusterConfig.executorMemory)
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

    def setSparkArgs(args: SparkArgumentParser.SparkOptionsMultiMap): SparkLauncher =
      args.filterKeys(key =>
        !disabledKeys.contains(key)
      ).foldLeft(self) { (sparkLauncher, keyValue) =>
        val (key, value) = keyValue
        val isNoValueArgument = value == null
        if (isNoValueArgument) {
          sparkLauncher.addSparkArg(key)
        } else {
          value.foldLeft(sparkLauncher) { case (sparkLauncher, v) =>
            sparkLauncher.addSparkArg(key, v)
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

