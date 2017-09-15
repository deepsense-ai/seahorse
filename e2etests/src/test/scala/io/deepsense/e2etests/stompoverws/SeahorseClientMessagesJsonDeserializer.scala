package io.deepsense.e2etests.stompoverws

import io.deepsense.models.json.graph.GraphJsonProtocol.GraphReader
import io.deepsense.models.json.workflow.ExecutionReportJsonProtocol
import io.deepsense.models.workflows.ExecutionReport
import io.deepsense.workflowexecutor.communication.message.global.{Heartbeat, HeartbeatJsonProtocol}
import io.deepsense.workflowexecutor.communication.mq.json.Constants.MessagesTypes
import io.deepsense.workflowexecutor.communication.mq.json.{DefaultJsonMessageDeserializer, JsonMQDeserializer}
import io.deepsense.workflowexecutor.communication.mq.serialization.json.WorkflowProtocol

private [stompoverws]  object SeahorseClientMessagesJsonDeserializer
    extends ExecutionReportJsonProtocol
    with HeartbeatJsonProtocol {

  // TODO Implement InferredState reader
  case class Deserializer(graphReader: GraphReader)
    extends JsonMQDeserializer(Seq(
      new DefaultJsonMessageDeserializer[ExecutionReport](WorkflowProtocol.executionReport),
      new DefaultJsonMessageDeserializer[Heartbeat](MessagesTypes.heartbeat)
    ))

}