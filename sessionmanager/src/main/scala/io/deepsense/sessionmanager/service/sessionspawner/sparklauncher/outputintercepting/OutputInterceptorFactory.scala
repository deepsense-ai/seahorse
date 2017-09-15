/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.sessionspawner.sparklauncher.outputintercepting

import java.io.File
import java.text.SimpleDateFormat
import java.util.logging._
import java.util.{Calendar, UUID}

import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.spark.launcher.SparkLauncher

import io.deepsense.sessionmanager.rest.requests.ClusterDetails

/**
  * Spark launcher is hardcoded to redirect child process output to parents
  * process java.util.logging Logger.
  *
  * Easiest way to intercept child process logs is to hook logger handler to
  * the logger SparkLauncher writes to.
  *
  * This class tries to hide the fact, that output intercepting is done via
  * hooking file handler to a specific logger.
  */
class OutputInterceptorFactory @Inject()(
  @Named("session-executor.spark-applications-logs-dir") val executorsLogDirectory: String
) {

  case class OutputInterceptorHandle private [OutputInterceptorFactory](
    private val logger: Logger,
    private val childProcLoggerName: String
  ) {
    def attachTo(sparkLauncher: SparkLauncher): Unit = {
      sparkLauncher.setConf(
        "spark.launcher.childProcLoggerName", childProcLoggerName
      )
    }

    def writeOutput(text: String): Unit = {
      logger.info(text)
    }

  }

  def prepareInterceptorWritingToFiles(clusterDetails: ClusterDetails): OutputInterceptorHandle = {
    new File(executorsLogDirectory).mkdirs()

    val childProcLoggerName = s"WE-app-${UUID.randomUUID()}"
    val logger = Logger.getLogger(s"org.apache.spark.launcher.app.$childProcLoggerName")

    val fileName = {
      val time = Calendar.getInstance().getTime()
      val format = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss")
      val formattedTime = format.format(time)
      s"$formattedTime-${clusterDetails.name}.log"
    }
    val handler = new FileHandler(s"$executorsLogDirectory/$fileName")
    handler.setFormatter(new SimpleFormaterWithoutOutputRedirectorNoise)
    logger.addHandler(handler)
    OutputInterceptorHandle(logger, childProcLoggerName)
  }

  class SimpleFormaterWithoutOutputRedirectorNoise extends Formatter {

    val simpleFormatter = new SimpleFormatter

    override def format(logRecord: LogRecord): String = {
      val formatted = simpleFormatter.format(logRecord)
      val redirectorNoise = "org.apache.spark.launcher.OutputRedirector redirect\nINFO: "
      val beginningOfRedirectorNoise = formatted.indexOf(redirectorNoise)

      val endOfRedirectorNoise = if (beginningOfRedirectorNoise > 0) {
        beginningOfRedirectorNoise + redirectorNoise.length
      } else {
        0
      }

      formatted.substring(endOfRedirectorNoise)
    }
  }

}
