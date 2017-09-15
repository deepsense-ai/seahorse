/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Rafal Hryciuk
 */

package io.deepsense.deeplang

import java.net.URI

import com.typesafe.config.ConfigFactory
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hdfs.DFSClient
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.BeforeAndAfterAll

import io.deepsense.deeplang.dataframe.{DataFrame, DataFrameBuilder}
import io.deepsense.entitystorage.{EntityStorageClient, EntityStorageClientFactory, EntityStorageClientFactoryImpl}

/**
 * Adds features to facilitate integration testing using Spark and entitystorage
 */
trait DOperationIntegTestSupport extends UnitSpec with BeforeAndAfterAll {

  val hdfsPath = "hdfs://ds-dev-env-master:8020"

  var executionContext: ExecutionContext = _

  var sparkConf: SparkConf = _
  var sparkContext: SparkContext = _
  var sqlContext: SQLContext = _
  var hdfsClient: DFSClient = _
  var entityStorageClientFactory: EntityStorageClientFactoryImpl = _


  override def beforeAll: Unit = {
    sparkConf = new SparkConf().setMaster("local[4]").setAppName("TestApp")
    sparkContext = new SparkContext(sparkConf)
    sqlContext = new SQLContext(sparkContext)
    hdfsClient = new DFSClient(new URI(hdfsPath), new Configuration())

    executionContext = new ExecutionContext
    executionContext.sqlContext = sqlContext
    executionContext.dataFrameBuilder = DataFrameBuilder(sqlContext)
    entityStorageClientFactory = EntityStorageClientFactoryImpl()
    executionContext.entityStorageClient = createEntityStorageClient(entityStorageClientFactory)
    executionContext.tenantId = "testTenantId"
  }

  override def afterAll: Unit = {
    sparkContext.stop()
    entityStorageClientFactory.close()
  }

  protected def assertDataFramesEqual(dt1: DataFrame, dt2: DataFrame): Unit = {
    assert(dt1.sparkDataFrame.schema == dt2.sparkDataFrame.schema)
    val collectedRows1: Array[Row] = dt1.sparkDataFrame.collect()
    val collectedRows2: Array[Row] = dt2.sparkDataFrame.collect()
    collectedRows1 should be (collectedRows2)
  }

  private def createEntityStorageClient(entityStorageClientFactory: EntityStorageClientFactory)
  : EntityStorageClient = {
    val config = ConfigFactory.load("entitystorage-communication.conf")
    val actorSystemName = config.getString("entityStorage.actorSystemName")
    val hostName = config.getString("entityStorage.hostname")
    val port = config.getInt("entityStorage.port")
    val actorName = config.getString("entityStorage.actorName")
    val timeoutSeconds = config.getInt("entityStorage.timeoutSeconds")
    entityStorageClientFactory.create(actorSystemName, hostName, port, actorName, timeoutSeconds)
  }
}
