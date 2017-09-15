/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters

import scala.collection._
import scalaz.Scalaz._
import scalaz._

import org.apache.spark.launcher.SparkLauncher

import io.deepsense.commons.models.ClusterDetails
import io.deepsense.sessionmanager.service.sessionspawner._
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.{SparkLauncherConfig, SparkLauncherError, UnexpectedException}

object SeahorseSparkLauncher {

  def apply(
      sessionConfig: SessionConfig,
      sparkLauncherConfig: SparkLauncherConfig,
      clusterConfig: ClusterDetails) = handleUnexpectedExceptions {
      for {
        sparkLauncher <- clusterConfig.clusterType match {
          case ClusterType.local =>
            LocalSparkLauncher(sessionConfig, sparkLauncherConfig, clusterConfig)
          case ClusterType.standalone =>
            StandaloneSparkLauncher(sessionConfig, sparkLauncherConfig, clusterConfig)
          case ClusterType.yarn =>
            YarnSparkLauncher(sessionConfig, sparkLauncherConfig, clusterConfig)
          case ClusterType.mesos =>
            MesosSparkLauncher(sessionConfig, sparkLauncherConfig, clusterConfig)
        }
      } yield sparkLauncher
        .setConfOpt("spark.executor.memory", clusterConfig.executorMemory)
        .setConfOpt("spark.driver.memory", clusterConfig.driverMemory)
        .setConfOpt("spark.executor.cores", clusterConfig.executorCores.map(_.toString))
        .setConfOpt("spark.cores.max", clusterConfig.totalExecutorCores.map(_.toString))
        .setConfOpt("spark.executor.instances", clusterConfig.numExecutors.map(_.toString))
        .setConf("spark.driver.extraJavaOptions", "-Dfile.encoding=UTF8")
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

    def mergeConfOption(key: String,
                        value: String,
                        args: Map[String, String],
                        delimiter: String = ","): SparkLauncher = {

      val selectedConfOption = args.filter {
        case(k, v) => (("--conf" == k) && (v != null))
      }.values.collect {
        case v if v.split("=", 2)(0) == key => v.split("=", 2)(1)
      }

      selectedConfOption match {
        case Nil => self.setConf(key, value)
        case head::rest => selectedConfOption.foldLeft(self) {
          case (akk, head) =>
            akk.setConf(key, s"$head$delimiter$value")
        }
      }

    }

    def setSparkArgs(args: Map[String, String]): SparkLauncher =
      args.filterKeys(key =>
        !disabledKeys.contains(key)
      ).foldLeft(self) { (sparkLauncher, keyValue) =>
        val (key, value) = keyValue
        val isNoValueArgument = value == null
        if (isNoValueArgument) {
          sparkLauncher.addSparkArg(key)
        } else {
          sparkLauncher.addSparkArg(key, value)
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

