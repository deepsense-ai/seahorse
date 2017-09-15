/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.graph

object Status extends Enumeration {
  type Status = Value
  val InDraft, Queued, Running, Completed, Failed, Aborted = Value
}
