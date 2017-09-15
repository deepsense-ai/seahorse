/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters

import scalaz._

import org.apache.spark.launcher.SparkLauncher

import io.deepsense.sessionmanager.rest.requests.ClusterDetails
import io.deepsense.sessionmanager.service.sessionspawner._
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.SparkLauncherConfig
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark.SparkAgumentParser
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark.SparkAgumentParser.UnknownOption

object SeahorseSparkLauncher {

  def apply(
      sessionConfig: SessionConfig,
      sparkLauncherConfig: SparkLauncherConfig,
      clusterConfig: ClusterDetails): Validation[UnknownOption, SparkLauncher] = {
    for {
      args <- SparkAgumentParser.parse(clusterConfig.params)
    } yield {
      val sparkLauncher = clusterConfig.clusterType match {
        case "local" => LocalSparkLauncher(sessionConfig, sparkLauncherConfig, clusterConfig)
        case "standalone" =>
          StandaloneSparkLauncher(sessionConfig, sparkLauncherConfig, clusterConfig)
        case "yarn" => YarnSparkLauncher(sessionConfig, sparkLauncherConfig, clusterConfig)
        case "mesos" => MesosSparkLauncher(sessionConfig, sparkLauncherConfig, clusterConfig)
      }

      sparkLauncher
        .setConfOpt("spark.executor.memory", clusterConfig.executorMemory)
        .setConfOpt("spark.executor.cores", clusterConfig.executorCores.map(_.toString))
        .setConfOpt("spark.executor.instances", clusterConfig.numExecutors.map(_.toString))
        .setSparkArgs(args.toMap)
    }
  }

  private implicit class RichSparkLauncher(self: SparkLauncher) {

    def setConfOpt(key: String, valueOpt: Option[String]): SparkLauncher = {
      valueOpt match {
        case Some(value) => self.setConf(key, value)
        case None => self
      }
    }

    def setSparkArgs(args: Map[String, String]): SparkLauncher =
      args.foldLeft(self) { (sparkLauncher, keyValue) =>
        val (key, value) = keyValue
        val isNoValueArgument = value == null
        if (isNoValueArgument) {
          sparkLauncher.addSparkArg(key)
        } else {
          sparkLauncher.addSparkArg(key, value)
        }
      }

  }

}

