/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons.cassandra

import java.util.concurrent.TimeUnit

import scala.concurrent.duration
import scala.concurrent.duration.FiniteDuration

import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest.time.{Millis, Seconds, Span}

import io.deepsense.commons.StandardSpec

trait CassandraTestSupport {
  suite: StandardSpec =>

  def cassandraTableName : String
  def cassandraKeySpaceName : String

  implicit override val patienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(5, Millis))
  val operationDuration = new FiniteDuration(5, duration.SECONDS)

  EmbeddedCassandraServerHelper.startEmbeddedCassandra()

  val host = "localhost"
  val port = 9142
  val user = "cassandra"
  val password = "cassandra"
  val oneSecond = TimeUnit.SECONDS.toMillis(1)

  val clusterFactory = new ClusterFactory
  val sessionFactory = new SessionFactory
  val cluster = clusterFactory.create(host, port, user, password, oneSecond)
  val session = {
    val temporarySession = cluster.connect()
    temporarySession.execute(s"CREATE KEYSPACE IF NOT EXISTS $cassandraKeySpaceName WITH" +
      " replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};")
    temporarySession.close()
    sessionFactory.connect(cluster, cassandraKeySpaceName)
  }
}
