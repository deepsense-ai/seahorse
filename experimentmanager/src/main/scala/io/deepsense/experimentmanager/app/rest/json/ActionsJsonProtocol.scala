/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app.rest.json

import spray.httpx.SprayJsonSupport
import spray.json._

import io.deepsense.experimentmanager.app.rest.actions.{AbortAction, Action, LaunchAction}
import io.deepsense.graphjson.NodeJsonProtocol

trait ActionsJsonProtocol
  extends DefaultJsonProtocol
  with SprayJsonSupport
  with NodeJsonProtocol {

  implicit val launchActionFormat = jsonFormat1(LaunchAction.apply)
  implicit val abortActionFormat = jsonFormat1(AbortAction.apply)

  implicit object ActionJsonFormat extends RootJsonReader[Action] {
    val abortName = "abort"
    val launchName = "launch"
    override def read(json: JsValue): Action = json match {
      case JsObject(x) if x.contains(abortName) =>
        x.get(abortName).get.convertTo[AbortAction]
      case JsObject(x) if x.contains(launchName) =>
        x.get(launchName).get.convertTo[LaunchAction]
      case x => deserializationError(s"Expected Abort Action or Launch Action, but got $x")
    }
  }
}

object ActionsJsonProtocol extends ActionsJsonProtocol
