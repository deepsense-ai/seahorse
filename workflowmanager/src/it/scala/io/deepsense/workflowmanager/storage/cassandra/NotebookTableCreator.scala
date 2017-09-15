/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage.cassandra

import com.datastax.driver.core.Session

object NotebookTableCreator {
  def create(table: String, session: Session) = {
    session.execute(createTableCommand(table))
  }

  private def createTableCommand(table: String): String = {
    s"create table if not exists $table (" +
      s"""
         workflow_id uuid,
         node_id uuid,
         notebook text,
         primary key (workflow_id, node_id)
      );
      """
  }
}
