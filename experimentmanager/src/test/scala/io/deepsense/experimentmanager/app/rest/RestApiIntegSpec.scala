/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Wojciech Jurczyk
 */

package io.deepsense.experimentmanager.app.rest

import java.util.UUID

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, duration}

import com.google.common.base.Function
import net.codingwell.scalaguice.KeyExtensions._
import net.codingwell.scalaguice._
import org.jclouds.ContextBuilder
import org.jclouds.compute.ComputeServiceContext
import org.jclouds.domain.Credentials
import org.jclouds.openstack.keystone.v2_0.domain.Access
import org.scalatest.BeforeAndAfter
import spray.routing.Route

import io.deepsense.experimentmanager.IntegTestSupport
import io.deepsense.experimentmanager.app.models.Experiment
import io.deepsense.experimentmanager.app.storage.ExperimentStorage
import io.deepsense.experimentmanager.auth.HasTenantId

class RestApiIntegSpec extends RestApiSpec with IntegTestSupport with BeforeAndAfter {
  suite: IntegTestSupport =>

  var experimentA: Experiment = null
  var experimentB: Experiment = null
  var experimentStorage: ExperimentStorage = null

  override def experimentOfTenantA = experimentA
  override def experimentOfTenantB = experimentB

  override def tenantAId: String = tenantId("userA")

  override def tenantBId: String = tenantId("userB")

  /**
   * A valid Auth Token of a user of tenant A. This user has to have roles
   * for all actions in ExperimentManager
   */
  override def validAuthTokenTenantA: String = validAuthToken("userA")

  /**
   * A valid Auth Token of a user of tenant B. This user has to have no roles.
   */
  override def validAuthTokenTenantB: String = validAuthToken("userB")

  override protected def testRoute: Route = getRestServiceInstance

  before {
    experimentA = Experiment(
      UUID.randomUUID(),
      tenantAId,
      "Experiment of Tenant A")

    experimentB = Experiment(
      UUID.randomUUID(),
      tenantBId,
      "Experiment of Tenant B")

    experimentStorage = getInstance[ExperimentStorage]
    Await.ready(experimentStorage.save(experimentA), FiniteDuration(2, duration.SECONDS))
    Await.ready(experimentStorage.save(experimentB), FiniteDuration(2, duration.SECONDS))
  }

  after {
    experimentStorage.delete(experimentA.id)
    experimentStorage.delete(experimentB.id)
  }

  private def tenantId(user: String): String = accessForUser(user).getToken.getTenant.get.getId

  private def validAuthToken(user: String): String = accessForUser(user).getToken.getId

  private def accessForUser(user: String): Access = {
    val identity = getConfig.getString(s"test.restapi.$user.identity")
    val password = getConfig.getString(s"test.restapi.$user.password")
    val context = ContextBuilder.newBuilder("openstack-nova")
      .endpoint(getConfig.getString("auth-service.endpoint"))
      .credentials(identity, password)
      .buildView(classOf[ComputeServiceContext])

    val auth = context
      .utils()
      .injector()
      .getInstance(typeLiteral[Function[Credentials, Access]].toKey)

    auth(
      new Credentials.Builder[Credentials]()
        .identity(identity).credential(password).build())
  }
}
