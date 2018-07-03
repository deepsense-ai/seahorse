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

package ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.spark

import java.util

import org.apache.spark.launcher._
import org.apache.commons.exec._
import scalaz._
import Scalaz._

import ai.deepsense.commons.utils.Logging
import ai.deepsense.commons.collection.MultiMap._
import ai.deepsense.sessionmanager.service.sessionspawner.sparklauncher.SparkLauncherError

object SparkArgumentParser extends Logging {
  import scala.collection.JavaConversions._


  type SparkOptionsMultiMap = MultiMap[String, String]

  implicit class SparkOptionsOp(self: SparkOptionsMultiMap) {
    def updateConfOptions(
      key: String,
      value: String,
      delimiter: String = ","): SparkArgumentParser.SparkOptionsMultiMap = {

      val updateOptions = (oldOptions: Set[String]) => oldOptions.foldLeft(self) {
        case (newArgs, oldOption) => {
          val oldValue = oldOption.split("=", 2)(1)
          val newValue = s"$key=$oldValue$delimiter$value"
          newArgs.removeBinding("--conf", oldOption).addBinding("--conf", newValue)
        }
      }
      val singleValue = s"$key=$value"
      self.get("--conf") match {
        case Some(null) => self.addBinding("--conf", singleValue)
        case None => self.addBinding("--conf", singleValue)
        case Some(set: Set[String]) => {
          val oldElements = set.filter(_.split("=", 2)(0) == key)
          if (oldElements.isEmpty) {
            self.addBinding("--conf", singleValue)
          } else {
            updateOptions(oldElements)
          }
        }
      }
    }

    def getConfOption(key: String): Option[Set[String]] = self.get("--conf") match {
      case Some(null) => None
      case None => None
      case Some(set: Set[String]) => {
        val options = set.filter(_.split("=", 2)(0) == key)
        if (options.isEmpty) {
          None
        } else {
          val values = options.map(_.split("=", 2)(1))
          Some(values)
        }
      }
    }
  }

  def parse(paramsStringOpt: Option[String]): Validation[UnknownOption, SparkOptionsMultiMap] = {
    paramsStringOpt match {
      case Some("") | None => Map.empty[String, Set[String]].success
      case Some(paramsString) => parse(paramsString)
    }
  }

  def parse(paramsString: String): Validation[UnknownOption, SparkOptionsMultiMap] = {
    val paramsStringNoNewlines = paramsString.replace("\n", " ")
    val executable = "fake-executable-to-prevent-cutting-of-first-argument"
    val command = CommandLine.parse(s"$executable $paramsStringNoNewlines")
    val args = command.getArguments

    // `Apache commons exec`s argument tokenization is not fully bash-sh like.
    // It properly treats quoted string as tokens, but keeps quotes as part of token.
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

  private class SparkAgumentParser extends PublicSparkSubmitOptionParser {

    var arguments = Map.empty[String, Set[String]]

    override def handle(opt: String, value: String): Boolean = {
      if (value == null) {
        arguments += ((opt, null))
      } else {
        arguments = arguments.addBinding(opt, value)
      }
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

  case class UnknownOption(opt: String) extends SparkLauncherError(s"Unknown opt $opt")

}
