/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager.rest

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
import io.deepsense.deeplang.catalogs.doperations.DOperationsCatalog
import io.deepsense.deeplang.doperables.Transformation
import io.deepsense.deeplang.doperations.MathematicalOperation
import io.deepsense.deeplang.{DOperation, DOperationCategories}
import io.deepsense.graph.{Graph, Node}
import io.deepsense.graphjson.GraphJsonProtocol.GraphReader
import io.deepsense.models.workflows.Workflow
import io.deepsense.workflowmanager.conversion.FileConverter
import io.deepsense.workflowmanager.storage.WorkflowStorage
import io.deepsense.workflowmanager.{WorkflowManagerIntegTestSupport, WorkflowTableCreator}

class WorkflowsApiIntegSpec
    extends WorkflowsApiSpec
    with WorkflowManagerIntegTestSupport
    with CassandraTestSupport
    with BeforeAndAfter {

  override def cassandraTableName: String = "experiments"
  override def cassandraKeySpaceName: String = "experimentmanager"

  var workflowA: Workflow = null
  var workflowAN: Workflow = null
  var workflowB: Workflow = null
  var workflowStorage: WorkflowStorage = null

  lazy val validTokenA = validAuthToken("userA")
  lazy val validTokenB = validAuthToken("userB")
  lazy val apiPrefixFromConfig = getConfig.getString("experiments.api.prefix")
  lazy val tenantA = tenantId("userA")
  lazy val tenantB = tenantId("userB")

  override def experimentOfTenantA: Workflow = workflowA
  override def experimentOfTenantAWithNode = workflowAN
  override def experimentOfTenantB: Workflow = workflowB

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
    WorkflowTableCreator.create(cassandraTableName, session)

    catalog.registerDOperation[MathematicalOperation](
      DOperationCategories.DataManipulation,
      "Performs a mathematical operation")

    dOperableCatalog.registerDOperable[Transformation]()

    workflowA = Workflow(
      Workflow.Id.randomId,
      tenantAId,
      "Experiment of Tenant A",
      Graph(),
      created,
      updated)

    workflowAN = Workflow(
      Workflow.Id.randomId,
      tenantAId,
      "Experiment of Tenant A with node",
      Graph(Set(Node(nodeUUID, prepareMathematicalOperation())), Set())
    )

    workflowB = Workflow(
      Workflow.Id.randomId,
      tenantBId,
      "Experiment of Tenant B",
      Graph(),
      created,
      updated)

    workflowStorage = getInstance[WorkflowStorage]
    import scala.concurrent.duration._
    Await.ready(workflowStorage.save(workflowA), 2.seconds)
    Await.ready(workflowStorage.save(workflowAN), 2.seconds)
    Await.ready(workflowStorage.save(workflowB), 2.seconds)
  }

  after {
    workflowStorage.delete(tenantAId, workflowA.id)
    workflowStorage.delete(tenantBId, workflowB.id)
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
    operation.formulaParam.value = Some("2")
    operation
  }
}
