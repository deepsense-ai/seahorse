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
 * Adds features to aid integration testing using HDFS
 */
trait HdfsIntegTestSupport
  extends FlatSpec
  with Matchers
  with OptionValues
  with Inside
  with Inspectors
  with BeforeAndAfterAll {

  val uberJarFilename = BuildInfo.name + "-assembly-" + BuildInfo.version + ".jar"

  val geUberJarPath = s"target/scala-2.11/$uberJarFilename"

  private val config = new Configuration()

  val testDir = "/tests/GeIntegrationTest"

  var cli: Option[DFSClient] = None

  override def beforeAll(): Unit = {
    // TODO: Configuration resource access should follow proper configuration access convention
    config.addResource("conf/hadoop/core-site.xml")
    config.addResource("conf/hadoop/yarn-site.xml")
    config.addResource("conf/hadoop/hdfs-site.xml")
    cli = Some(new DFSClient(
      new URI("hdfs://" + Constants.MasterHostname + ":" + Constants.HdfsNameNodePort),
      config))

    cli.get.delete(testDir, true)
    cli.get.mkdirs(testDir, new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.ALL), true)
    copy(geUberJarPath, s"$testDir/$uberJarFilename")
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
  def copy(localFrom: String, remoteTo: String): Unit = {
    val localFromFile = new File(localFrom)
    if (localFromFile.isDirectory) {
      cli.get.mkdirs(remoteTo, null, true)
      localFromFile.listFiles.foreach(f => copy(f.getPath, remoteTo + "/" + f.getName))
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
}
