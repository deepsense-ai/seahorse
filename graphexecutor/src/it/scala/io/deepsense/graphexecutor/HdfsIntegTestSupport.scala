/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Grzegorz Chilkiewicz
 */
package io.deepsense.graphexecutor

import java.net.URI

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.permission.{FsAction, FsPermission}
import org.apache.hadoop.hdfs.DFSClient
import org.scalatest._

import io.deepsense.deeplang.DSHdfsClient
import io.deepsense.graphexecutor.deployment.DeployOnHdfs

/**
 * Adds features to aid integration testing using HDFS.
 * NOTE: beforeAll method deploys current deepsense build on HDFS cluster.
 */
trait HdfsIntegTestSupport
  extends FlatSpec
  with Matchers
  with OptionValues
  with Inside
  with Inspectors
  with BeforeAndAfterAll {

  private val config = new Configuration()

  var cli: Option[DFSClient] = None
  var dsHdfsClient: Option[DSHdfsClient] = None

  override def beforeAll(): Unit = {
    // TODO: Configuration resource access should follow proper configuration access convention
    config.addResource(getClass().getResource("/conf/hadoop/core-site.xml"))
    config.addResource(getClass().getResource("/conf/hadoop/yarn-site.xml"))
    config.addResource(getClass().getResource("/conf/hadoop/hdfs-site.xml"))
    import HdfsForIntegTestsProperties._
    cli = Some(new DFSClient( new URI("hdfs://" + MasterHostname + ":" + HdfsNameNodePort), config))
    dsHdfsClient = Some(new DSHdfsClient(cli.get))

    cli.get
      .mkdirs(Constants.TestDir, new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.ALL), true)
    DeployOnHdfs.deployOnHdfs(dsHdfsClient.get)
  }

  override def afterAll(): Unit = {
    info("If some tests failed, please:")
    info("1) Make sure that You have Development Environment running on Your machine")
    info("2) Make sure that You have entry '172.28.128.100 " +
      HdfsForIntegTestsProperties.MasterHostname + "' in /etc/hosts")
    info("3) Make sure that You can establish connection with " +
      HdfsForIntegTestsProperties.MasterHostname)
    info("4) Make sure that You are using Java 'openjdk-7-jdk'")
    cli.map(_.close())
  }

  /**
   * Copies file or entire directory (recursively) from local file system to remote HDFS.
   * @param localFrom local file path to copy from
   * @param remoteTo remote file path to copy to
   */
  def copyFromLocal(localFrom: String, remoteTo: String): Unit = {
    dsHdfsClient.get.copyLocalFile(localFrom, remoteTo)
  }

  /**
   * Copies example DataFrame to HDFS
   */
  def copyDataFrameToHdfs(): Unit = {
    cli.get.delete(SimpleGraphExecutionIntegSuiteEntities.dataFrameLocation, true)
    copyFromLocal(
      "../graphexecutor/src/test/resources/SimpleDataFrame",
      SimpleGraphExecutionIntegSuiteEntities.dataFrameLocation)
  }
}
