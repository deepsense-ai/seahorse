/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.entitystorage

import com.datastax.driver.core.Session

object EntitiesTableCreator {
  def create(table: String, session: Session) = session.execute(createTableCommand(table))

  private def createTableCommand(table: String): String =
    s"create table if not exists $table (" +
      """
      tenantid text,
      id uuid,
      name text,
      description text,
      dclass text,
      created timestamp,
      updated timestamp,
      url text,
      saved boolean,
      report text,
      primary key (tenantid, id)
      );
      """
}
