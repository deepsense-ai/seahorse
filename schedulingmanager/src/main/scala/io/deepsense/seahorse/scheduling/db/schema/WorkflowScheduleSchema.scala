/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.seahorse.scheduling.db.schema

import java.sql.JDBCType
import java.util.UUID

import slick.jdbc.{PositionedParameters, SetParameter}

import io.deepsense.seahorse.scheduling.SchedulingManagerConfig
import io.deepsense.seahorse.scheduling.db.Database
import io.deepsense.commons.service.db.CommonSlickFormats

object WorkflowScheduleSchema {

  import Database.api._
  import CommonSlickFormats._

  case class WorkflowScheduleDB(
    id: UUID,
    cron: String,
    workflowId: UUID,
    emailForReports: String
  )

  final class WorkflowScheduleTable(tag: Tag)
      extends Table[WorkflowScheduleDB](tag, Some(SchedulingManagerConfig.database.schema), "workflow_schedule") {
    def id = column[UUID]("id", O.PrimaryKey)
    def cron = column[String]("cron")
    def workflowId = column[UUID]("workflow_id")
    def emailForReports = column[String]("email_for_reports")

    def * = (id, cron, workflowId, emailForReports) <> (WorkflowScheduleDB.tupled, WorkflowScheduleDB.unapply)
  }

  lazy val workflowScheduleTable = TableQuery[WorkflowScheduleTable]
}

// sbt-native-package won't work with multiple Mains
// https://github.com/sbt/sbt-native-packager/pull/319
// TODO use sbt-docker with sbt-assembly and define mainClass in assembly as
// it's solved in Neptune
/*
object PrintDDL extends App {
  import Database.api._
  import WorkflowScheduleSchema._
  // scalastyle:off println
  println(workflowScheduleTable.schema.createStatements.mkString("\n"))
  // scalastyle:on println
}
*/
