package io.deepsense.e2etests.stompoverws

import asia.stampy.common.message.{StampyMessage, StompMessageType}
import asia.stampy.common.parsing.StompMessageParser
import asia.stampy.server.message.message.MessageMessage

import io.deepsense.commons.utils.Logging
import io.deepsense.e2etests.WorkflowParser
import io.deepsense.models.workflows.{ExecutionReport, InferredState}
import io.deepsense.workflowexecutor.communication.message.global.Heartbeat
import io.deepsense.workflowexecutor.communication.mq.json.Global

private [stompoverws] object ExecutionReportParser extends Logging {

  def parse(rawStompFrame: String): Option[ExecutionReport] = {
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

  private def parser = new StompMessageParser()
  private def deserializer = SeahorseClientMessagesJsonDeserializer.Deserializer(WorkflowParser.graphReader)

}
