/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.graph

object Status extends Enumeration {
  type Status = Value
  val Draft = Value(0, "DRAFT")
  val Queued = Value(1, "QUEUED")
  val Running = Value(2, "RUNNING")
  val Completed = Value(3, "COMPLETED")
  val Failed = Value(4, "FAILED")
  val Aborted = Value(5, "ABORTED")
}
