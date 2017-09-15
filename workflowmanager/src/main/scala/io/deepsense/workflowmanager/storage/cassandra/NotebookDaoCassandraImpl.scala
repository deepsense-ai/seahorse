/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage.cassandra

import scala.concurrent.{ExecutionContext, Future}

import com.datastax.driver.core.{Row, Session}
import com.datastax.driver.core.querybuilder.QueryBuilder._
import com.datastax.driver.core.querybuilder.{QueryBuilder, Select, Update}
import com.google.inject.Inject
import com.google.inject.name.Named

import io.deepsense.commons.models.Id
import io.deepsense.commons.utils.Logging
import io.deepsense.workflowmanager.storage.NotebookStorage

class NotebookDaoCassandraImpl @Inject() (
    @Named("cassandra.workflowmanager.notebook.table") table: String,
    @Named("WorkflowsSession") session: Session)
    (implicit ec: ExecutionContext)
  extends NotebookStorage
  with Logging {

  override def get(id: Id): Future[Option[String]] = {
    Future(session.execute(getQuery(id))).map(rs => {
      val rows = rs.all()
      rows.size match {
        case 0 => None
        case 1 => Some(rows.get(0).getString("notebook"))
        case n =>
          logger.error(s"Query returned ${n} rows for notebook with id ${id.value}")
          throw new IllegalStateException(s"Multiple notebooks with id ${id.value}")
      }
    })
  }

  override def save(id: Id, notebook: String): Future[Unit] = {
    Future(session.execute(updateQuery(id, notebook)))
  }

  private def getQuery(id: Id): Select.Where = {
    QueryBuilder.select("id", "notebook").from(table)
      .where(QueryBuilder.eq(WorkflowRowMapper.Id, id.value))
  }

  private def updateQuery(id: Id, notebook: String): Update.Where = {
    QueryBuilder.update(table).`with`()
      .and(set("notebook", notebook))
      .where(QueryBuilder.eq(WorkflowRowMapper.Id, id.value))
  }
}
