/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.scheduling.db.dbio

import java.util.UUID

import io.deepsense.seahorse.scheduling.converters.SchedulesConverters
import io.deepsense.seahorse.scheduling.db.Database
import io.deepsense.seahorse.scheduling.db.schema.WorkflowScheduleSchema

object WorkflowSchedulesDBIOs {

  import scala.concurrent.ExecutionContext.Implicits.global

  import Database.api._
  import WorkflowScheduleSchema._

  def getAllForWorkflow(workflowId: UUID) = for {
    datasourceDbs <- workflowScheduleTable.filter(_.workflowId === workflowId).result
  } yield datasourceDbs.map(SchedulesConverters.fromDb).toList

}
