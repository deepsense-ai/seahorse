/**
 * Copyright (c) 2015, CodiLime Inc.
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

  class EntityStorageClientFactoryMock(
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
    override def close(): Unit = {
    }
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

  @Provides
  @Singleton
  @Named("BikesIntegSuiteEntities")
  def provideForBikesIntegSuiteEntities: EntityStorageClientFactory = {
    new EntityStorageClientFactoryMock(Map(
      (BikesIntegSuiteEntities.bikesTenantId, BikesIntegSuiteEntities.demand.id)
        -> BikesIntegSuiteEntities.demand,
      (BikesIntegSuiteEntities.bikesTenantId, BikesIntegSuiteEntities.weather.id)
        -> BikesIntegSuiteEntities.weather
    ))
  }
}

object GraphExecutorTest {
  /**
   * Directory on HDFS used for testing purposes
   */
  val TestDir = "/tests"

  /**
   * Tenant id used for testing purposes
   */
  val TestTenantId = "TestTenantId"
}

object SimpleGraphExecutionIntegSuiteEntities {
  val Name = "SimpleGraphExecutionIntegSuite"

  val entityTenantId = GraphExecutorTest.TestTenantId
  val entityId = Entity.Id.fromString("87cced7a-2c47-4210-9243-245284f74d72")
  val dataFrameLocation = GraphExecutorTest.TestDir + "/" + entityId

  val entity = Entity(
    entityTenantId,
    entityId,
    "testEntity name",
    "testEntity description",
    "DataFrame",
    Some(DataObjectReference(dataFrameLocation)),
    Some(DataObjectReport("testEntity Report")),
    DateTime.now,
    DateTime.now,
    saved = true)
}

object BikesIntegSuiteEntities {
  val Name = "BikesIntegSuiteEntities"

  val bikesTenantId = GraphExecutorTest.TestTenantId

  val demandId: Entity.Id = Entity.Id.fromString("52df8588-b192-4446-a418-2c95f8a94d85")
  val demandLocation = GraphExecutorTest.TestDir + "/" + demandId

  val demand = Entity(
    bikesTenantId,
    demandId,
    "demandEntity name",
    "demandEntity description",
    "DataFrame",
    Some(DataObjectReference(demandLocation)),
    Some(DataObjectReport("demandEntity Report")),
    DateTime.now,
    DateTime.now,
    saved = true)

  val weatherId: Entity.Id = Entity.Id.fromString("7ff0e089-8059-491c-9a7e-557349633312")
  val weatherLocation = GraphExecutorTest.TestDir + "/" + weatherId

  val weather = Entity(
    bikesTenantId,
    weatherId,
    "weatherEntity name",
    "weatherEntity description",
    "DataFrame",
    Some(DataObjectReference(weatherLocation)),
    Some(DataObjectReport("weatherEntity Report")),
    DateTime.now,
    DateTime.now,
    saved = true)
}
