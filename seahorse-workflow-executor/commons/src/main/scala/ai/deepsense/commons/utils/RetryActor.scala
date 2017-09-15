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

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success, Try}

import akka.actor.{Actor, ActorRef, Status}

class RetryActor[T](
    retryInterval: FiniteDuration,
    retryCountLimit: Int,
    workCode: => Future[T],
    workDescription: Option[String]) extends Actor
    with Logging {

  import RetryActor._

  private implicit val ec: ExecutionContext = context.system.dispatcher

  override def receive: Receive = {
    case Trigger => doWork(sender, 0)
    case Retry(initialSender, retryCount) => doWork(initialSender, retryCount)
  }

  val workDescriptionForLogs: String = workDescription.map(" " + _).getOrElse(" some work")

  private def doWork(initialSender: ActorRef, retryCount: Int): Unit = {
    workCode.onComplete {
      case Success(t) => initialSender ! t
      case Failure(RetriableException(msg, cause)) if retryCount < retryCountLimit =>
        logFailure(msg, cause)
        logger.info(s"Will retry$workDescriptionForLogs in $retryInterval.")
        context.system.scheduler.scheduleOnce(retryInterval, self, Retry(initialSender, retryCount + 1))
      case Failure(RetriableException(msg, cause)) if retryCount >= retryCountLimit =>
        logFailure(msg, cause)
        val retryLimitReachedException =
          RetryLimitReachedException(s"Retry limit of $retryCountLimit reached, last error was $cause", cause)
        logger.error(s"Retry limit reached for$workDescriptionForLogs.", retryLimitReachedException)
        initialSender ! Status.Failure(retryLimitReachedException)
      case Failure(f) =>
        logFailure(f.getMessage, Some(f))
        logger.error(s"Unexpected exception when performing$workDescriptionForLogs.", f)
        initialSender ! Status.Failure(f)
    }
  }

  private def logFailure(msg: String, tOpt: Option[Throwable]): Unit = {
    val msgText = s"Exception when performing$workDescriptionForLogs. The message was: $msg"
    tOpt match {
      case Some(t) => logger.info(msgText, t)
      case None => logger.info(msgText)
    }
  }
}

object RetryActor {
  sealed trait Message
  case object Trigger extends Message
  case class Retry(initialSender: ActorRef, retryCount: Int) extends Message

  case class RetryLimitReachedException(msg: String, lastError: Option[Throwable]) extends Exception(msg)
  case class RetriableException(msg: String, cause: Option[Throwable]) extends Exception(msg, cause.orNull)

}
