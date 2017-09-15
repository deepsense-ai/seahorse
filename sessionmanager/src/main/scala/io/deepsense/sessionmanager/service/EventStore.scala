/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.sessionmanager.service

import scala.concurrent.Future

import org.joda.time.DateTime

import io.deepsense.commons.models.{ClusterDetails, Id}
import io.deepsense.sessionmanager.service.EventStore._

trait EventStore {

  /**
    * Returns the last event (if any) that occurred for a workflow.
    */
  def getLastEvent(workflowId: Id): Future[Option[Event]]

  /**
    * Returns all last events for all known workflows as a map.
    */
  def getLastEvents: Future[Map[Id, Event]]

  /**
    * Records a Heartbeat event in the EventStore.
    * In a health system heartbeat events can be recorded only if a Started
    * event was recorded in the past.
    * @param workflowId The id of the workflow that the event links to.
    * @return If no previous event was recorded for the workflow specified
    *         by the id, then the method returns Left(InvalidWorkflowId()).
    */
  def heartbeat(workflowId: Id): Future[Either[InvalidWorkflowId, Unit]]

  /**
    * Records a Started event in the EventStore.
    * The event is recorded only if there in no recorded events for the specified
    * workflow.
    * @param workflowId The id of the workflow that the event links to.
    * @return If there is an event recorded for the workflow (meaning that a session
    *         for the workflow exists), then it returns Left(SessionExists()).
    */
  def started(workflowId: Id, clusterDetails: ClusterDetails): Future[Either[SessionExists, Unit]]

  /**
    * Removes events for the specified workflow from the store.
    * @param workflowId The id of the workflow that the event links to.
    * @return Always return Unit.
    */
  def killed(workflowId: Id): Future[Unit]
}

object EventStore {
  case class InvalidWorkflowId()
  case class SessionExists()

  sealed trait Event {
    def workflowId: Id
    def happenedAt: DateTime
    def cluster: ClusterDetails
  }

  case class Started(workflowId: Id, happenedAt: DateTime, cluster: ClusterDetails)
      extends Event
  case class HeartbeatReceived(workflowId: Id, happenedAt: DateTime, cluster: ClusterDetails)
      extends Event
}
