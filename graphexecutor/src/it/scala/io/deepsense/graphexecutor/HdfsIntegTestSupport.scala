/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Grzegorz Chilkiewicz
 */
package io.deepsense.graphexecutor

import java.io.{BufferedInputStream, BufferedOutputStream, File, FileInputStream}
import java.net.URI

import buildinfo.BuildInfo
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.permission.{FsAction, FsPermission}
import org.apache.hadoop.hdfs.DFSClient
import org.scalatest._

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

  val uberJarFilename = BuildInfo.name + "-assembly-" + BuildInfo.version + ".jar"

  val geUberJarPath = s"../graphexecutor/target/scala-2.11/$uberJarFilename"

  private val config = new Configuration()

  var cli: Option[DFSClient] = None

  override def beforeAll(): Unit = {
    // TODO: Configuration resource access should follow proper configuration access convention
    config.addResource("conf/hadoop/core-site.xml")
    config.addResource("conf/hadoop/yarn-site.xml")
    config.addResource("conf/hadoop/hdfs-site.xml")
    cli = Some(new DFSClient(
      new URI("hdfs://" + Constants.MasterHostname + ":" + Constants.HdfsNameNodePort),
      config))

    cli.get.delete(s"/$uberJarFilename", true)
    cli.get.delete(s"/graphexecutor.conf", true)
    cli.get
      .mkdirs(Constants.TestDir, new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.ALL), true)
    // NOTE: We assume here that uber-jar has been assembled immediately before test task
    copyFromLocal(geUberJarPath, s"/$uberJarFilename")
    copyFromLocal(
      "../graphexecutor/src/main/resources/graphexecutor.conf",
      "/graphexecutor.conf")
  }

  override def afterAll(): Unit = {
    info("If some tests failed, please:")
    info("1) Make sure that You have Development Environment running on Your machine")
    info("2) Make sure that You have entry '172.28.128.100 " + Constants.MasterHostname +
      "' in /etc/hosts")
    info("3) Make sure that You can establish connection with " + Constants.MasterHostname)
    info("4) Make sure that You are using Java 'openjdk-7-jdk'")
    cli.map(_.close())
  }

  /**
   * Copies file or entire directory (recursively) from local file system to remote HDFS.
   * @param localFrom local file path to copy from
   * @param remoteTo remote file path to copy to
   */
  def copyFromLocal(localFrom: String, remoteTo: String): Unit = {
    val localFromFile = new File(localFrom)
    if (localFromFile.isDirectory) {
      cli.get.mkdirs(remoteTo, null, true)
      localFromFile.listFiles.foreach(f => copyFromLocal(f.getPath, remoteTo + "/" + f.getName))
    } else {
      val inputStream = new BufferedInputStream(new FileInputStream(localFrom))
      try {
        val fos = new BufferedOutputStream(cli.get.create(remoteTo, false))
        try {
          org.apache.commons.io.IOUtils.copy(inputStream, fos)
        } finally {
          fos.close()
        }
      } finally {
        inputStream.close()
      }
    }
  }

  /**
   * Copies example DataFrame to hdfs
   */
  def copyDataFrameToHdfs(): Unit = {
    cli.get.delete(SimpleGraphExecutionIntegSuiteEntities.dataFrameLocation, true)
    copyFromLocal(
      "../graphexecutor/src/test/resources/SimpleDataFrame",
      SimpleGraphExecutionIntegSuiteEntities.dataFrameLocation)
  }
}
