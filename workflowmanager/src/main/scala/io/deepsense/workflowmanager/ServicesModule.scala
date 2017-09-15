/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.workflowmanager

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

import com.google.inject.AbstractModule
import com.google.inject.assistedinject.FactoryModuleBuilder

import io.deepsense.entitystorage.{EntityStorageClient, EntityStorageClientFactory}
import io.deepsense.models.entities.{CreateEntityRequest, Entity, EntityWithData}
import io.deepsense.workflowmanager.storage.cassandra.WorkflowDaoCassandraModule

/**
 * Configures services.
 */
class ServicesModule extends AbstractModule {
  override def configure(): Unit = {
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
