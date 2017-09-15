/**
 * Copyright (c) 2015, CodiLime, Inc.
 */

package io.deepsense.experimentmanager.rest

import scala.concurrent.Await

import com.google.common.base.Function
import com.google.inject.{Key, TypeLiteral}
import org.jclouds.ContextBuilder
import org.jclouds.compute.ComputeServiceContext
import org.jclouds.domain.Credentials
import org.jclouds.openstack.keystone.v2_0.domain.Access
import org.scalatest.BeforeAndAfter
import spray.routing.Route

import io.deepsense.experimentmanager.ExperimentManagerIntegTestSupport
import io.deepsense.experimentmanager.storage.ExperimentStorage
import io.deepsense.graph.Graph
import io.deepsense.models.experiments.Experiment

class ExperimentsApiIntegSpec
  extends ExperimentsApiSpec
  with ExperimentManagerIntegTestSupport
  with BeforeAndAfter {

  var experimentA: Experiment = null
  var experimentB: Experiment = null
  var experimentStorage: ExperimentStorage = null

  lazy val validTokenA = validAuthToken("userA")
  lazy val validTokenB = validAuthToken("userB")
  lazy val apiPrefixFromConfig = getConfig.getString("experiments.api.prefix")
  lazy val tenantA = tenantId("userA")
  lazy val tenantB = tenantId("userB")

  override def experimentOfTenantA = experimentA
  override def experimentOfTenantB = experimentB

  override val tenantAId: String = tenantA

  override val tenantBId: String = tenantB

  /**
   * A valid Auth Token of a user of tenant A. This user has to have roles
   * for all actions in ExperimentManager
   */
  override val validAuthTokenTenantA: String = validTokenA

  /**
   * A valid Auth Token of a user of tenant B. This user has to have no roles.
   */
  override val validAuthTokenTenantB: String = validTokenB

  override protected def testRoute: Route = getRestServiceInstance

  override val apiPrefix: String = apiPrefixFromConfig

  before {
    experimentA = Experiment(
      Experiment.Id.randomId,
      tenantAId,
      "Experiment of Tenant A",
      Graph())

    experimentB = Experiment(
      Experiment.Id.randomId,
      tenantBId,
      "Experiment of Tenant B",
      Graph())

    experimentStorage = getInstance[ExperimentStorage]
    import scala.concurrent.duration._
    Await.ready(experimentStorage.save(experimentA), 2.seconds)
    Await.ready(experimentStorage.save(experimentB), 2.seconds)
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
      .getInstance(Key.get(new TypeLiteral[Function[Credentials, Access]](){}))

    auth(
      new Credentials.Builder[Credentials]()
        .identity(identity).credential(password).build())
  }
}
