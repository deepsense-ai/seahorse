/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark

import java.util

import scala.collection._

import org.apache.spark.launcher._

import io.deepsense.commons.utils.Logging

object SparkAgumentParser extends Logging {
  import scala.collection.JavaConversions._

  def parse(paramsString: String): Map[String, String] = {
    val parser = new SparkAgumentParser()
    parser.parseArgs(paramsString.split("\\s+").toList)
    parser.arguments
  }

  private class SparkAgumentParser extends PublicSparkSubmitOptionParser {

    val arguments = mutable.Map.empty[String, String]

    override def handle(opt: String, value: String): Boolean = {
      arguments.put(opt, value)
      true
    }
    override def handleUnknown(opt: String): Boolean = {
      throw new IllegalArgumentException(s"Uknown opt: $opt")
    }
    override def handleExtraArgs(extra: util.List[String]): Unit = {
      logger.warn(s"Handle extra args: ${extra.mkString(", ")}")
    }
  }

}
