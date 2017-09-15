/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.livy.requests

case class Create(
  file: String,
  className: String,
  args: Seq[String],
  files: Seq[String],
  conf: Map[String, String]
)
