/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark

import java.util

import scala.collection._

import org.apache.spark.launcher._
import org.apache.commons.exec._

import scalaz._
import Scalaz._

import io.deepsense.commons.utils.Logging
import io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.SparkLauncherError

object SparkAgumentParser extends Logging {
  import scala.collection.JavaConversions._

  def parse(paramsStringOpt: Option[String]): Validation[UnknownOption, Map[String, String]] = {
    paramsStringOpt match {
      case Some("") | None => Map.empty[String, String].success
      case Some(paramsString) => parse(paramsString)
    }
  }

  def parse(paramsString: String): Validation[UnknownOption, Map[String, String]] = {
    val executable = "fake-executable-to-prevent-cutting-of-first-argument"
    val command = CommandLine.parse(s"$executable $paramsString")
    val args = command.getArguments

    // `Apache commons exec`s argument tokenization is not fully bash-sh like.
    // It propertly treat quoted string as tokens, but keeps quotes as part of token.
    // Relevant stack overflow thread - http://goo.gl/I5zirJ
    // TODO Make it more bash-sh like
    val argsWithFixedQuotes = args.map { argument =>
      if (argument.startsWith("\"") && argument.endsWith("\"")) {
        argument.substring(1, argument.length - 1)
      } else {
        argument
      }
    }

    try {
      val parser = new SparkAgumentParser()
      parser.parseArgs(argsWithFixedQuotes.toList)
      parser.arguments.success
    } catch {
      case unknownOptions: UnknownOption => unknownOptions.failure
    }
  }

  def parseAsMap(paramsOption: Option[String]): Validation[UnknownOption, Map[String, String]] = {
    parse(paramsOption).map(_.toMap)
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

  case class UnknownOption(opt: String) extends SparkLauncherError(s"Uknown opt $opt")

}
