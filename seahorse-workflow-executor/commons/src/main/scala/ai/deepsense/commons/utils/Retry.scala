/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.commons.utils

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

trait Retry[T] {

  def work: Future[T]

  def retryInterval: FiniteDuration
  def retryLimit: Int
  def actorSystem: ActorSystem
  // the timeout should exceed the retryLimit * retryInterval + (retryLimit + 1) * avgWorkDuration
  // otherwise the ask in tryWork method may timeout before all the retries have been attempted
  implicit def timeout: Timeout
  def workDescription: Option[String]

  private lazy val retryActor = actorSystem.actorOf(Props(new RetryActor[T](
    retryInterval,
    retryLimit,
    work,
    workDescription
  )))

  def tryWork: Future[T] = (retryActor ? RetryActor.Trigger).asInstanceOf[Future[T]]

}
