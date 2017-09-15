/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service.livy.responses

object BatchState extends Enumeration {
  type BatchState = Value
  val Ok = Value("ok")
  val NotStarted = Value("not_started")
  val Starting = Value("starting")
  val Idle = Value("idle")
  val Running = Value("running")
  val Busy = Value("busy")
  val ShuttingDown = Value("shutting_down")
  val Error = Value("error")
  val Dead = Value("dead")
  val Success = Value("success")
}
