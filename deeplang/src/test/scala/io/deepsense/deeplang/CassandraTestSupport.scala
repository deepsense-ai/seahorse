/**
 * Copyright 2015, deepsense.io
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

package io.deepsense.deeplang

import java.util.concurrent.TimeUnit

import scala.concurrent.blocking

import com.datastax.driver.core.{Cluster, Session}
import com.datastax.driver.core.policies.ConstantReconnectionPolicy
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.apache.commons.lang3.StringUtils

trait CassandraTestSupport {

  def cassandraTableName: String

  def cassandraKeySpaceName: String

  EmbeddedCassandraServerHelper.startEmbeddedCassandra(TimeUnit.MINUTES.toMillis(1))

  val host = "localhost"
  val port = 9142
  val user = "cassandra"
  val password = "cassandra"
  val tenSeconds = TimeUnit.SECONDS.toMillis(10)

  val cluster = create(host, port, user, password, tenSeconds)
  val session = {
    val temporarySession = cluster.connect()
    temporarySession.execute(s"CREATE KEYSPACE IF NOT EXISTS $cassandraKeySpaceName WITH" +
      " replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};")
    temporarySession.close()
    connect(cluster, cassandraKeySpaceName)
  }

  def create(
      host: String,
      port: Int,
      user: String,
      password: String,
      reconnectDelay: Long): Cluster = {
    require(StringUtils.isNoneBlank(host), "Cassandra cluster's host can not be empty")
    require(StringUtils.isNoneBlank(user), "Cassandra cluster's user can not be empty")
    require(StringUtils.isNoneBlank(password), "Cassandra cluster's password can not be empty")
    Cluster.builder()
      .addContactPoint(host)
      .withPort(port)
      .withoutJMXReporting()
      .withoutMetrics()
      .withCredentials(user, password)
      .withReconnectionPolicy(new ConstantReconnectionPolicy(reconnectDelay))
      .build()
  }

  def connect(cluster: Cluster, keySpace: String): Session = {
    require(StringUtils.isNoneBlank(keySpace), "Cassandra cluster's keyspace can not be empty")
    blocking {
      val session = cluster.connect()
      session.execute(s"USE $keySpace;")
      session
    }
  }
}
