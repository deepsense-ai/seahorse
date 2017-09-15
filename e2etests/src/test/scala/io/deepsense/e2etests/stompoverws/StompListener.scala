package io.deepsense.e2etests.stompoverws

import scala.collection.mutable.MutableList
import scala.util.Try

import org.jfarcand.wcs.TextListener

import io.deepsense.commons.utils.Logging
import io.deepsense.models.workflows.ExecutionReport

class StompListener(
    val websocketMessages: MutableList[String] = new MutableList,
    val executionReports: MutableList[ExecutionReport] = new MutableList
) extends TextListener with Logging {

  // TODO Find and use dedicated library for Stomp over WS

  override def onMessage(message: String): Unit = {

    logger.debug(s"Received raw message $message")
    websocketMessages += message
    for {
      stompFrame <- StompFramesFromWebsocketFrame(message)
      executionReport <- Try {
        ExecutionReportParser.parse(stompFrame)
      } match {
        case scala.util.Success(report) => report
        case scala.util.Failure(e) => {
          // TODO Implement all messages deserializers.
          logger.warn("Error when parsing message", e)
          None
        }
      }
    } {
      executionReports += executionReport
    }
  }

}