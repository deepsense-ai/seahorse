/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons.exception.json

import io.deepsense.commons.exception.{FailureCode, DeepSenseFailure, FailureDescription}
import io.deepsense.commons.json.{EnumerationSerializer, UUIDJsonProtocol}

import spray.json._

trait FailureDescriptionJsonProtocol extends DefaultJsonProtocol with UUIDJsonProtocol {
  implicit val failureCodeFormat = EnumerationSerializer.jsonEnumFormat(FailureCode)
  implicit val failureIdFormat = jsonFormat1(DeepSenseFailure.Id.apply)
  implicit val failureDescriptionFormat = jsonFormat5(FailureDescription.apply)
}
