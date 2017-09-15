/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.graph.nodestate

import org.joda.time.DateTime

import ai.deepsense.commons.datetime.DateTimeConverter
import ai.deepsense.commons.exception.FailureDescription
import ai.deepsense.commons.models.Entity
import ai.deepsense.commons.models.Entity.Id
import ai.deepsense.graph.nodestate.name.NodeStatusName

sealed abstract class NodeStatus(
    val name: NodeStatusName,
    val results: Seq[Entity.Id])
  extends Serializable {

  def start: Running
  def finish(results: Seq[Id]): Completed
  def fail(error: FailureDescription): Failed
  def abort: Aborted
  def enqueue: Queued

  // Sugar.

  def isDraft: Boolean = this match {
    case Draft(_) => true
    case _ => false
  }

  def isQueued: Boolean = this match {
    case Queued(_) => true
    case _ => false
  }

  def isRunning: Boolean = this match {
    case Running(_, _) => true
    case _ => false
  }

  def isCompleted: Boolean = this match {
    case Completed(_, _, _) => true
    case _ => false
  }

  def isAborted: Boolean = this match {
    case Aborted(_) => true
    case _ => false
  }

  def isFailed: Boolean = this match {
    case Failed(_, _, _) => true
    case _ => false
  }

  protected def stateCannot(what: String): IllegalStateException =
    new IllegalStateException(s"State ${this.getClass.getSimpleName} cannot $what()")
}

final case class Draft(override val results: Seq[Entity.Id] = Seq.empty)
  extends NodeStatus(NodeStatusName.Draft, results) {

  override def start: Running = throw stateCannot("start")
  override def finish(results: Seq[Entity.Id]): Completed = throw stateCannot("finish")
  override def abort: Aborted = Aborted(results)
  override def fail(error: FailureDescription): Failed = {
    val now = DateTimeConverter.now
    Failed(now, now, error)
  }
  override def enqueue: Queued = Queued(results)
}

final case class Queued(override val results: Seq[Entity.Id] = Seq.empty)
  extends NodeStatus(NodeStatusName.Queued, results) {

  override def start: Running = Running(DateTimeConverter.now, results)
  override def finish(results: Seq[Entity.Id]): Completed = throw stateCannot("finish")
  override def abort: Aborted = Aborted(results)
  override def fail(error: FailureDescription): Failed = {
    val now = DateTimeConverter.now
    Failed(now, now, error)
  }
  override def enqueue: Queued = throw stateCannot("enqueue")
}

final case class Running(started: DateTime, override val results: Seq[Entity.Id] = Seq.empty)
    extends NodeStatus(NodeStatusName.Running, results) {
  override def start: Running = throw stateCannot("start")
  override def abort: Aborted = Aborted(results)
  override def fail(error: FailureDescription): Failed =
    Failed(started, DateTimeConverter.now, error)
  override def finish(results: Seq[Id]): Completed =
    Completed(started, DateTimeConverter.now, results)
  override def enqueue: Queued = throw stateCannot("enqueue")
}

sealed trait FinalState {
  self: NodeStatus =>
  override def start: Running = throw stateCannot("start")
  override def finish(results: Seq[Id]): Completed = throw stateCannot("finish")
  override def abort: Aborted = throw stateCannot("abort")
  override def fail(error: FailureDescription): Failed = throw stateCannot("fail")
  override def enqueue: Queued = throw stateCannot("enqueue")
}

final case class Completed(started: DateTime, ended: DateTime, override val results: Seq[Id])
  extends NodeStatus(NodeStatusName.Completed, results) with FinalState
final case class Failed(started: DateTime, ended: DateTime, error: FailureDescription)
  extends NodeStatus(NodeStatusName.Failed, results = Seq.empty) with FinalState
final case class Aborted(override val results: Seq[Entity.Id] = Seq.empty)
  extends NodeStatus(NodeStatusName.Aborted, results)
  with FinalState
