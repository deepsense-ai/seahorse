/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.commons.cassandra

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.policies.ConstantReconnectionPolicy
import org.apache.commons.lang3.StringUtils

class ClusterFactory {
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
}
