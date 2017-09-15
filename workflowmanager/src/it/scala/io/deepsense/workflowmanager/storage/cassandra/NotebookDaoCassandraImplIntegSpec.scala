/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage.cassandra

import scala.concurrent.{Await, Future}

import com.datastax.driver.core.querybuilder.QueryBuilder
import org.apache.commons.lang3.RandomStringUtils
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, Matchers}

import io.deepsense.commons.StandardSpec
import io.deepsense.commons.cassandra.CassandraTestSupport
import io.deepsense.commons.models.Id
import io.deepsense.commons.utils.Logging

class NotebookDaoCassandraImplIntegSpec
  extends StandardSpec
  with ScalaFutures
  with MockitoSugar
  with Matchers
  with BeforeAndAfter
  with CassandraTestSupport
  with GraphJsonTestSupport with Logging {

  var notebooksDao: NotebookDaoCassandraImpl = _

  val n1@(notebook1Id, notebook1) = createNotebook()
  val n2@(notebook2Id, notebook2) = createNotebook()
  val n3@(notebook3Id, notebook3) = createNotebook()

  val storedNotebooks = Set(n1, n2)

  def cassandraTableName: String = "notebooks"
  def cassandraKeySpaceName: String = "workflowmanager"

  before {
    NotebookTableCreator.create(cassandraTableName, session)
    notebooksDao = new NotebookDaoCassandraImpl(cassandraTableName, session)
  }

  "NotebooksDao" should {

    "find notebook by id" in withStoredNotebooks(storedNotebooks) {
      whenReady(notebooksDao.get(notebook1Id)) { notebook =>
        notebook shouldBe Some(notebook1)
      }
    }

    "return None if notebook does not exist" in withStoredNotebooks(storedNotebooks) {
      whenReady(notebooksDao.get(notebook3Id)) { notebook =>
        notebook shouldBe None
      }
    }

    "create notebook" in withStoredNotebooks(storedNotebooks) {
      whenReady(notebooksDao.save(notebook3Id, notebook3)) { _ =>
        whenReady(notebooksDao.get(notebook3Id)) { notebook =>
          notebook shouldBe Some(notebook3)
        }
      }
    }

    "update notebook" in withStoredNotebooks(storedNotebooks) {
      val modifiedNotebook2 = "modified"
      whenReady(notebooksDao.save(notebook2Id, modifiedNotebook2)) { _ =>
        whenReady(notebooksDao.get(notebook2Id)) { notebook =>
          notebook shouldBe Some(modifiedNotebook2)
        }
      }
    }
  }

  private def withStoredNotebooks(
      storedNotebooks: Set[(Id, String)])(testCode: => Any): Unit = {
    val s = Future.sequence(storedNotebooks.map {
      case (id, notebook) => notebooksDao.save(id, notebook)
    })
    Await.ready(s, operationDuration)
    try {
      testCode
    } finally {
      session.execute(QueryBuilder.truncate(cassandraTableName))
    }
  }

  def createNotebook(): (Id, String) = {
    (Id.randomId, RandomStringUtils.randomAlphanumeric(16))
  }
}
