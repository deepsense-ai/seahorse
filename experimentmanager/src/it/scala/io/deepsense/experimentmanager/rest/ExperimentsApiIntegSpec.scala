/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.experimentmanager.rest

import scala.concurrent.Await

import com.google.common.base.Function
import com.google.inject._
import com.google.inject.util.Modules
import org.jclouds.ContextBuilder
import org.jclouds.compute.ComputeServiceContext
import org.jclouds.domain.Credentials
import org.jclouds.openstack.keystone.v2_0.domain.Access
import org.scalatest.BeforeAndAfter
import spray.routing.Route

import io.deepsense.commons.cassandra.CassandraTestSupport
import io.deepsense.deeplang.{DOperationCategories, DOperation}
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.deeplang.doperables.Transformation
import io.deepsense.deeplang.doperations.MathematicalOperation
import io.deepsense.experimentmanager.conversion.FileConverter
import io.deepsense.experimentmanager.storage.ExperimentStorage
import io.deepsense.experimentmanager.{ExperimentManagerIntegTestSupport, ExperimentsTableCreator}
import io.deepsense.graph.{Graph, Node}
import io.deepsense.graphjson.GraphJsonProtocol.GraphReader
import io.deepsense.models.experiments.Experiment

class ExperimentsApiIntegSpec
    extends ExperimentsApiSpec
    with ExperimentManagerIntegTestSupport
    with CassandraTestSupport
    with BeforeAndAfter {

  override def cassandraTableName: String = "experiments"
  override def cassandraKeySpaceName: String = "experimentmanager"

  var experimentA: Experiment = null
  var experimentAN: Experiment = null
  var experimentB: Experiment = null
  var experimentStorage: ExperimentStorage = null

  lazy val validTokenA = validAuthToken("userA")
  lazy val validTokenB = validAuthToken("userB")
  lazy val apiPrefixFromConfig = getConfig.getString("experiments.api.prefix")
  lazy val tenantA = tenantId("userA")
  lazy val tenantB = tenantId("userB")

  override def experimentOfTenantA: Experiment = experimentA
  override def experimentOfTenantAWithNode = experimentAN
  override def experimentOfTenantB: Experiment = experimentB

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

  override protected def appGuiceModule : Module =
    Modules.`override`(super.appGuiceModule).`with`(new AbstractModule {
      override def configure(): Unit = {}
      @Singleton
      @Provides
      def provideGraphReader(dOperationsCatalog: DOperationsCatalog): GraphReader = graphReader

      @Provides
      def provideFileConverter: FileConverter = mock[FileConverter]
    })

  before {
    ExperimentsTableCreator.create(cassandraTableName, session)

    catalog.registerDOperation[MathematicalOperation](
      DOperationCategories.DataManipulation,
      "Performs a mathematical operation")

    dOperableCatalog.registerDOperable[Transformation]()

    experimentA = Experiment(
      Experiment.Id.randomId,
      tenantAId,
      "Experiment of Tenant A",
      Graph(),
      created,
      updated)

    experimentAN = Experiment(
      Experiment.Id.randomId,
      tenantAId,
      "Experiment of Tenant A with node",
      Graph(Set(Node(nodeUUID, prepareMathematicalOperation())), Set())
    )

    experimentB = Experiment(
      Experiment.Id.randomId,
      tenantBId,
      "Experiment of Tenant B",
      Graph(),
      created,
      updated)

    experimentStorage = getInstance[ExperimentStorage]
    import scala.concurrent.duration._
    Await.ready(experimentStorage.save(experimentA), 2.seconds)
    Await.ready(experimentStorage.save(experimentAN), 2.seconds)
    Await.ready(experimentStorage.save(experimentB), 2.seconds)
  }

  after {
    experimentStorage.delete(tenantAId, experimentA.id)
    experimentStorage.delete(tenantBId, experimentB.id)
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

  private def prepareMathematicalOperation(): DOperation = {
    val operation = MathematicalOperation()
    operation.parameters.getStringParameter(MathematicalOperation.formulaParam).value = Some("2")
    operation
  }
}
