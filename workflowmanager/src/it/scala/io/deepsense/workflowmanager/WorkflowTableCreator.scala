/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager

import com.datastax.driver.core.Session

object WorkflowTableCreator {
  def create(table: String, session: Session) = {
    session.execute(createTableCommand(table))
    session.execute(createDeletedIndexCommand(table))
  }

  private def createTableCommand(table: String): String = {
    s"create table if not exists $table (" +
      s"""
         tenantId text,
         id uuid,
         name text,
         graph text,
         description text,
         state_status text,
         state_description text,
         created timestamp,
         updated timestamp,
         deleted boolean,
         primary key (tenantId, id)
      );
      """
  }

  private def createDeletedIndexCommand(table: String): String = {
    s"CREATE INDEX if not exists deleted_${table} ON ${table}(deleted);"
  }
}
