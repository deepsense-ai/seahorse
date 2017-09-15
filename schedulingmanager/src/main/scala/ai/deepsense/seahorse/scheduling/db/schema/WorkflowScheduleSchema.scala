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

package ai.deepsense.seahorse.scheduling.db.schema

import java.sql.JDBCType
import java.util.UUID

import slick.jdbc.{PositionedParameters, SetParameter}

import ai.deepsense.seahorse.scheduling.db.Database
import ai.deepsense.commons.service.db.CommonSlickFormats
import ai.deepsense.seahorse.scheduling.SchedulingManagerConfig

object WorkflowScheduleSchema {

  import Database.api._
  import CommonSlickFormats._

  case class WorkflowScheduleDB(
    id: UUID,
    cron: String,
    workflowId: UUID,
    emailForReports: String,
    presetId: Long
  )

  final class WorkflowScheduleTable(tag: Tag)
      extends Table[WorkflowScheduleDB](tag, Some(SchedulingManagerConfig.database.schema), "workflow_schedule") {
    def id = column[UUID]("id", O.PrimaryKey)
    def cron = column[String]("cron")
    def workflowId = column[UUID]("workflow_id")
    def emailForReports = column[String]("email_for_reports")
    def presetId = column[Long]("preset_id")

    def * = (id, cron, workflowId, emailForReports, presetId) <> (WorkflowScheduleDB.tupled, WorkflowScheduleDB.unapply)
  }

  lazy val workflowScheduleTable = TableQuery[WorkflowScheduleTable]
}

// sbt-native-package won't work with multiple Mains
// https://github.com/sbt/sbt-native-packager/pull/319
// TODO use sbt-assembly and define mainClass in assembly as it's solved in Neptune
/*
object PrintDDL extends App {
  import Database.api._
  import WorkflowScheduleSchema._
  // scalastyle:off println
  println(workflowScheduleTable.schema.createStatements.mkString("\n"))
  // scalastyle:on println
}
*/
