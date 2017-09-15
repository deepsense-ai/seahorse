/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app.rest.json


import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol

import io.deepsense.graphjson.{GraphKnowledgeJsonProtocol, NodeJsonProtocol}

/**
 * Defines how models are serialized to JSON and deserialized from it.
 */
trait RestJsonProtocol
  extends DefaultJsonProtocol
  with SprayJsonSupport
  with NodeJsonProtocol
  with GraphKnowledgeJsonProtocol
  with ActionsJsonProtocol
  with IdJsonProtocol
  with ExceptionsJsonProtocol
  with ExperimentJsonProtocol
