/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.clusters

import org.apache.spark.launcher.SparkLauncher

import io.deepsense.commons.models.Id
import io.deepsense.sessionmanager.rest.requests.ClusterDetails
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.SparkLauncherConfig
import io.deepsense.sessionmanager.service.sessionspawner._
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark.SparkAgumentParser

object SeahorseSparkLauncher {

  def apply(
      sessionConfig: SessionConfig,
      sparkLauncherConfig: SparkLauncherConfig,
      clusterConfig: ClusterDetails): SparkLauncher = {
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
      .setSparkArgsOpt(clusterConfig.params)
  }

  implicit class RichSparkLauncher(self: SparkLauncher) {

    def setConfOpt(key: String, valueOpt: Option[String]): SparkLauncher = {
      valueOpt match {
        case Some(value) => self.setConf(key, value)
        case None => self
      }
    }

    def setSparkArgsOpt(paramsOpt: Option[String]): SparkLauncher = paramsOpt match {
      case Some(params) =>
        val parsedSparkArgs = SparkAgumentParser.parse(params)
        parsedSparkArgs.foldLeft(self) { (sparkLauncher, keyValue) =>
          val (key, value) = keyValue
          val isNoValueArgument = value == null
          if(isNoValueArgument) {
            sparkLauncher.addSparkArg(key)
          } else {
            sparkLauncher.addSparkArg(key, value)
          }
        }
      case None => self
    }

  }

}

