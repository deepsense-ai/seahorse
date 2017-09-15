/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service

object Status extends Enumeration {
  type Status = Value
  val Running = Value("running")
  val Creating = Value("creating")
  val Error = Value("error")

}
