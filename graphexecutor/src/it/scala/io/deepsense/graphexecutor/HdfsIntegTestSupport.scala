/**
 * Copyright (c) 2015, CodiLime Inc.
 */
package io.deepsense.graphexecutor

import java.net.URI

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.permission.{FsAction, FsPermission}
import org.apache.hadoop.hdfs.DFSClient
import org.scalatest._

import io.deepsense.deeplang.DSHdfsClient

/**
 * Adds features to aid integration testing using HDFS.
 * NOTE: beforeAll method deploys current deepsense build on HDFS cluster.
 */
trait HdfsIntegTestSupport
  extends WordSpec
  with Matchers
  with OptionValues
  with Inside
  with Inspectors
  with BeforeAndAfterAll {

  private val config = new Configuration()

  var cli: Option[DFSClient] = None
  var dsHdfsClient: Option[DSHdfsClient] = None

  protected def requiredFiles: Map[String, String]

  protected def cleanupHdfs(): Unit = {
    requiredFiles.foreach { case (_, to) =>
      cli.get.delete(to, true)
    }
  }

  protected def copyFilesToHdfs(): Unit = {
    cleanupHdfs()
    requiredFiles.foreach { case (from, to) =>
      copyFromLocal(this.getClass.getResource(from).getPath, to)
    }
  }

  override def beforeAll(): Unit = {
    // TODO: Configuration resource access should follow proper configuration access convention
    config.addResource(getClass().getResource("/conf/hadoop/core-site.xml"))
    config.addResource(getClass().getResource("/conf/hadoop/yarn-site.xml"))
    config.addResource(getClass().getResource("/conf/hadoop/hdfs-site.xml"))
    import HdfsForIntegTestsProperties._
    cli = Some(new DFSClient(new URI("hdfs://" + MasterHostname + ":" + HdfsNameNodePort), config))
    dsHdfsClient = Some(new DSHdfsClient(cli.get))

    // Deploy test-specific files
    copyFilesToHdfs()
  }

  override def afterAll(): Unit = {
    info("If some tests failed, please:")
    info("1) Make sure that You have Development Environment running on Your machine")
    info(s"2) Make sure that You have entry ${HdfsForIntegTestsProperties.MasterIp} " +
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
    // Create directories for file on HDFS explicitly
    cli.get.mkdirs(
      remoteTo.substring(0, remoteTo.lastIndexOf("/")),
      new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.ALL),
      true)
    // Copy file to remote HDFS
    dsHdfsClient.get.copyLocalFile(localFrom, remoteTo)
  }
}
