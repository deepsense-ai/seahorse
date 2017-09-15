/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark

import java.util

import scala.collection._

import org.apache.spark.launcher._

import scalaz._
import Scalaz._

import io.deepsense.commons.utils.Logging

object SparkAgumentParser extends Logging {
  import scala.collection.JavaConversions._

  def parse(paramsStringOpt: Option[String]): Validation[UnknownOption, Map[String, String]] = {
    paramsStringOpt match {
      case Some("") | None => Map.empty[String, String].success
      case Some(paramsString) => parse(paramsString)
    }
  }

  def parse(paramsString: String): Validation[UnknownOption, Map[String, String]] = {
    val parser = new SparkAgumentParser()

    try {
      parser.parseArgs(paramsString.split("\\s+").toList)
      parser.arguments.success
    } catch {
      case unknownOptions: UnknownOption => unknownOptions.failure
    }
  }

  private class SparkAgumentParser extends PublicSparkSubmitOptionParser {

    val arguments = mutable.Map.empty[String, String]

    override def handle(opt: String, value: String): Boolean = {
      arguments.put(opt, value)
      true
    }
    override def handleUnknown(opt: String): Boolean = {
      throw UnknownOption(opt)
    }
    override def handleExtraArgs(extra: util.List[String]): Unit = {
      if (extra.nonEmpty) {
        logger.warn(s"Handle extra args: ${extra.mkString(", ")}")
      }
    }
  }

  case class UnknownOption(opt: String) extends Exception(s"Uknown opt $opt")

}
