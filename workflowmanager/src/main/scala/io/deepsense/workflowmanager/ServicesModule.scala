/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.Future

import com.google.inject.assistedinject.FactoryModuleBuilder
import com.google.inject.AbstractModule

import io.deepsense.entitystorage.{EntityStorageClient, EntityStorageClientFactoryImpl, EntityStorageClientFactory}
import io.deepsense.workflowmanager.execution.ExecutionModule
import io.deepsense.workflowmanager.storage.cassandra.WorkflowDaoCassandraModule
import io.deepsense.models.entities.{CreateEntityRequest, Entity, EntityWithData}

/**
 * Configures services.
 */
class ServicesModule extends AbstractModule {
  override def configure(): Unit = {
    install(new ExecutionModule)
    install(new WorkflowDaoCassandraModule)
    install(new FactoryModuleBuilder()
      .implement(classOf[WorkflowManager], classOf[WorkflowManagerImpl])
      .build(classOf[WorkflowManagerProvider]))

    bind(classOf[EntityStorageClientFactory])
      .to(classOf[StubEntityStorageClientFactory])
  }
}

class StubEntityStorageClientFactory extends EntityStorageClientFactory {

  override def create(actorSystemName: String, hostname: String,
    port: Int, actorName: String, timeoutSeconds: Int): EntityStorageClient = {
    new StubEntityStorageClient
  }

  override def close(): Unit = ()
}

class StubEntityStorageClient extends EntityStorageClient {

  override def createEntity(inputEntity: CreateEntityRequest)
    (implicit duration: FiniteDuration): Future[Entity.Id] = ???

  override def getEntityData(tenantId: String, id: Entity.Id)
    (implicit duration: FiniteDuration): Future[Option[EntityWithData]] = ???
}
