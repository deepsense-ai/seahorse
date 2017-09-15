/**
 * Copyright (c) 2015, CodiLime Inc.
 *
 * Owner: Grzegorz Chilkiewicz
 */
package io.deepsense.graphexecutor

import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides, Singleton}
import org.joda.time.DateTime

import io.deepsense.entitystorage.{EntityStorageClient, EntityStorageClientFactory, EntityStorageClientTestInMemoryImpl}
import io.deepsense.models.entities.{DataObjectReference, DataObjectReport, Entity}

class GraphExecutorTestModule extends AbstractModule {
  override def configure(): Unit = {
  }

  private class EntityStorageClientFactoryMock(
      initialState: Map[(String, Entity.Id), Entity] = Map())
    extends EntityStorageClientFactory {

    override def create(
        actorSystemName: String,
        hostname: String,
        port: Int,
        actorName: String,
        timeoutSeconds: Int): EntityStorageClient = {
      EntityStorageClientTestInMemoryImpl(initialState)
    }

    /**
     * Closes EntityStorageClientFactory. After close, create cannot be executed.
     */
    override def close(): Unit = ???
  }

  @Provides
  @Singleton
  @Named("SimpleGraphExecutionIntegSuite")
  def provideForSimpleGraphExecutionIntegSuite: EntityStorageClientFactory = {
    new EntityStorageClientFactoryMock(Map(
      (SimpleGraphExecutionIntegSuiteEntities.entityTenantId,
        SimpleGraphExecutionIntegSuiteEntities.entity.id)
        -> SimpleGraphExecutionIntegSuiteEntities.entity))
  }
}

object SimpleGraphExecutionIntegSuiteEntities {
  val Name = "SimpleGraphExecutionIntegSuite"

  val entityTenantId = Constants.TestTenantId
  val entityUuid = "87cced7a-2c47-4210-9243-245284f74d72"
  val dataFrameLocation = Constants.TestDir + "/" + entityUuid

  val entity = Entity(
    entityTenantId,
    entityUuid,
    "testEntity name",
    "testEntity description",
    "DataFrame",
    Some(DataObjectReference(dataFrameLocation)),
    Some(DataObjectReport("testEntity Report")),
    DateTime.now,
    DateTime.now,
    saved = true)
}
