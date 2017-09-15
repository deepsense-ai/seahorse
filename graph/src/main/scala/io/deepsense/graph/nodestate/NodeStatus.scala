/**
 * Copyright 2015, deepsense.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.deepsense.graph.nodestate

import org.joda.time.DateTime

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.exception.FailureDescription
import io.deepsense.commons.models.Entity
import Entity.Id

sealed abstract class NodeStatus(val name: String) extends Serializable {
  def start: Running
  def finish(results: Seq[Id]): Completed
  def fail(error: FailureDescription): Failed
  def abort: Aborted.type
  def enqueue: Queued.type

  // Sugar.

  def isDraft: Boolean = this match {
    case Draft => true
    case _ => false
  }

  def isQueued: Boolean = this match {
    case Queued => true
    case _ => false
  }

  def isRunning: Boolean = this match {
    case Running(_) => true
    case _ => false
  }

  def isCompleted: Boolean = this match {
    case Completed(_, _, _) => true
    case _ => false
  }

  def isAborted: Boolean = this match {
    case Aborted => true
    case _ => false
  }

  def isFailed: Boolean = this match {
    case Failed(_, _, _) => true
    case _ => false
  }

  protected def stateCannot(what: String): IllegalStateException =
    new IllegalStateException(s"State ${this.getClass.getSimpleName} cannot $what()")
}

case object Draft extends NodeStatus("DRAFT") {
  override def start: Running = throw stateCannot("start")
  override def finish(results: Seq[Entity.Id]): Completed = throw stateCannot("finish")
  override def abort: Aborted.type = Aborted
  override def fail(error: FailureDescription): Failed = throw stateCannot("fail")
  override def enqueue: Queued.type = Queued
}

case object Queued extends NodeStatus("QUEUED") {
  override def start: Running = Running(DateTimeConverter.now)
  override def finish(results: Seq[Entity.Id]): Completed = throw stateCannot("finish")
  override def abort: Aborted.type = Aborted
  override def fail(error: FailureDescription): Failed = {
    val now = DateTimeConverter.now
    Failed(now, now, error)
  }
  override def enqueue: Queued.type = throw stateCannot("enqueue")
}

final case class Running(started: DateTime) extends NodeStatus("RUNNING") {
  override def start: Running = throw stateCannot("start")
  override def abort: Aborted.type = Aborted
  override def fail(error: FailureDescription): Failed =
    Failed(started, DateTimeConverter.now, error)
  override def finish(results: Seq[Id]): Completed =
    Completed(started, DateTimeConverter.now, results)
  override def enqueue: Queued.type = throw stateCannot("enqueue")
}

sealed trait FinalState {
  self: NodeStatus =>
  override def start: Running = throw stateCannot("start")
  override def finish(results: Seq[Id]): Completed = throw stateCannot("finish")
  override def abort: Aborted.type = throw stateCannot("abort")
  override def fail(error: FailureDescription): Failed = throw stateCannot("fail")
  override def enqueue: Queued.type = throw stateCannot("enqueue")
}

final case class Completed(started: DateTime, ended: DateTime, results: Seq[Id])
  extends NodeStatus("COMPLETED") with FinalState
final case class Failed(started: DateTime, ended: DateTime, error: FailureDescription)
  extends NodeStatus("FAILED") with FinalState
case object Aborted extends NodeStatus("ABORTED") with FinalState

