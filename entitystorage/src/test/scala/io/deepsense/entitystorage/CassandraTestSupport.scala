/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.entitystorage

import scala.concurrent.duration
import scala.concurrent.duration.FiniteDuration

import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest.time.{Millis, Seconds, Span}

import io.deepsense.commons.StandardSpec
import io.deepsense.commons.cassandra.{SessionFactory, ClusterFactory}

trait CassandraTestSupport {
  suite: StandardSpec =>

  implicit override val patienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(5, Millis))
  val operationDuration = new FiniteDuration(5, duration.SECONDS)

  EmbeddedCassandraServerHelper.startEmbeddedCassandra()

  val host = "localhost"
  val port = 9142
  val user = "cassandra"
  val password = "cassandra"
  val table = "entities"
  val keySpace = "entitystorage"

  val clusterFactory = new ClusterFactory
  val sessionFactory = new SessionFactory
  val cluster = clusterFactory.create(host, port, user, password)
  val session = {
    val temporarySession = cluster.connect()
    temporarySession.execute(s"CREATE KEYSPACE IF NOT EXISTS $keySpace WITH" +
      " replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};")
    temporarySession.close()
    sessionFactory.connect(cluster, keySpace)
  }
}
