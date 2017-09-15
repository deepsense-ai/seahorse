package io.deepsense.e2etests.stompoverws

import scala.util.Try

import asia.stampy.common.message.{StampyMessage, StompMessageType}
import asia.stampy.common.parsing.StompMessageParser
import asia.stampy.server.message.message.MessageMessage
import org.apache.commons.lang.StringEscapeUtils

import io.deepsense.commons.utils.Logging
import io.deepsense.deeplang.CatalogRecorder
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.e2etests.WorkflowParser
import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.workflows.{ExecutionReport, InferredState}
import io.deepsense.workflowexecutor.communication.message.global.Heartbeat
import io.deepsense.workflowexecutor.communication.mq.json.Global

object ExecutionReportParser extends Logging {

  def parse(javaEscapedWebsocketMessage: String): Option[ExecutionReport] = {
    val websocketMessage = StringEscapeUtils.unescapeJava(javaEscapedWebsocketMessage)

    val isWebSocketHeartbeat = websocketMessage == "o" || websocketMessage == "h"
    val isSeahorseHeartbeat = websocketMessage.contains("heartbeat")

    if (isWebSocketHeartbeat || isSeahorseHeartbeat) {
      None
    } else {
      val rawStompFrame = trimWebsocketEnvelope(websocketMessage)
      val stompFrame: StampyMessage[_] = parser.parseMessage(rawStompFrame)

      if (stompFrame.getMessageType == StompMessageType.MESSAGE) {
        val stompMessageFrame = stompFrame.asInstanceOf[MessageMessage]
        val jsonBody = stompMessageFrame.getBody[String]

        val msg = deserializer.deserializeMessage(jsonBody.getBytes(Global.charset))
        msg match {
          case _: Heartbeat | InferredState => None
          case executionReport: ExecutionReport => Some(executionReport)
        }
      } else {
        None
      }
    }
  }

  // Websocket messages looks like:
  // a["$SOME_MESSAGE_BODY_HERE"]
  private def trimWebsocketEnvelope(websocketMessage: String): String = {
    val prefix = "a[\""
    val suffix = "\"]"
    websocketMessage.substring(prefix.length, websocketMessage.length - suffix.length)
  }

  private def parser = new StompMessageParser()
  private def deserializer = SeahorseClientMessagesJsonDeserializer.Deserializer(WorkflowParser.graphReader)

}
