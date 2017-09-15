/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.livy.requests

case class Create(
  file: String,
  className: String,
  args: Seq[String],
  jars: Seq[String],
  conf: Map[String, String]
)
