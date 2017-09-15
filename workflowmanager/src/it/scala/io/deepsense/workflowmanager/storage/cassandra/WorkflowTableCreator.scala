/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage.cassandra

import com.datastax.driver.core.Session

object WorkflowTableCreator {
  def create(table: String, session: Session) = {
    session.execute(createTableCommand(table))
    session.execute(createDeletedIndexCommand(table))
  }

  private def createTableCommand(table: String): String = {
    s"create table if not exists $table (" +
      s"""
         id uuid,
         workflow text,
         results text,
         last_execution_time text,
         deleted boolean,
         primary key (id)
      );
      """
  }

  private def createDeletedIndexCommand(table: String): String = {
    s"CREATE INDEX if not exists deleted_${table} ON ${table}(deleted);"
  }
}
