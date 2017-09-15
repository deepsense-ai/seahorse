/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage.cassandra

import com.datastax.driver.core.Session

object WorkflowStateStorageCreator {
  def createWorkflowStateTableCommand(table: String, session: Session): Unit =
    session.execute(s"""
                       |create table if not exists $table (
                       |  workflow_id uuid,
                       |  node_id uuid,
                       |  update_time timestamp,
                       |  state text,
                       |  reports text,
                       |  primary key(workflow_id, node_id)
                       |);
     """.stripMargin)

}
