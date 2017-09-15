/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons.cassandra

import scala.concurrent.blocking

import com.datastax.driver.core.{Session, Cluster}
import org.apache.commons.lang3.StringUtils

class SessionFactory {
  def connect(cluster: Cluster, keySpace: String): Session = {
    require(StringUtils.isNotBlank(keySpace), "Cassandra cluster's keyspace can not be empty")
    blocking {
      val session = cluster.connect()
      session.execute(s"USE $keySpace;")
      session
    }
  }
}
