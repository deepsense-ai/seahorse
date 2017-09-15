package io.deepsense.e2etests.stompoverws

import org.apache.commons.lang.StringEscapeUtils

private [stompoverws] object StompFramesFromWebsocketFrame {

  // One STOMP frame might also be fragmented into few WEBSOCKET frames.
  // We might need to handle it someday.

  def apply(websocketFrame: String): List[String] = {
    val isWebSocketHeartbeat = websocketFrame == "o" || websocketFrame == "h"

    if(isWebSocketHeartbeat) {
      Nil
    } else {
      // Websocket frame looks like this:
      // a["$STOMP_FRAME_1", "$STOMP_FRAME_2"]
      // Each stomp frame might contain escaped double quote in it
      //
      // This regex extract double quoted frames, but ignored escaped double quotes inside each frame.
      val regex = """(?<!\\)"(.+?)(?<!\\)""""r

      val stompFramesMatches = regex.findAllMatchIn(websocketFrame).toList

      val stompFrames = stompFramesMatches.map { frameMatch =>
        val frameMatchString = frameMatch.toString()
        val withoutDoubleQuotes = frameMatchString.substring(1, frameMatchString.length - 1)
        StringEscapeUtils.unescapeJava(withoutDoubleQuotes)
      }
      stompFrames
    }
  }

}
