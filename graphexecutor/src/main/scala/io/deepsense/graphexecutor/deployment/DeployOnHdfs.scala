/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graphexecutor.deployment

import java.net.URI

import buildinfo.BuildInfo
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.permission.{FsAction, FsPermission}
import org.apache.hadoop.hdfs.DFSClient

import io.deepsense.deeplang.DSHdfsClient

// scalastyle:off println

/**
 * Performs DeepSense.io deployment on external HDFS cluster.
 */
object DeployOnHdfs {
  val geConfig = ConfigFactory.load()

  val geJarFilename = BuildInfo.name + "-assembly-" + BuildInfo.version + ".jar"
  val geDepsJarFilename = BuildInfo.name + "-assembly-" + BuildInfo.version + "-deps.jar"

  val geJarPath = s"../graphexecutor/target/scala-2.11/$geJarFilename"
  val geDepsJarPath = s"../graphexecutor/target/scala-2.11/$geDepsJarFilename"
  val log4jXmlPath = s"../graphexecutor/src/main/resources/log4j.xml"

  val geConfPath =
    getClass().getResource("/" + geConfig.getString("deployment.etc.applicationconf.name")).getPath

  def main(args: Array[String]): Unit = {
    println("DeepSense.io deployment on HDFS starts")

    val deployGeWithDeps = args.length == 1 && args(0) == "deployGeWithDeps"

    val config = new Configuration()
    config.addResource(getClass().getResource("/conf/hadoop/core-site.xml"))
    config.addResource(getClass().getResource("/conf/hadoop/yarn-site.xml"))
    config.addResource(getClass().getResource("/conf/hadoop/hdfs-site.xml"))
    val dfsClient = new DFSClient(new URI(getHdfsAddressFromConfig()), config)
    val dsHdfsClient = new DSHdfsClient(dfsClient)
    deployOnHdfs(dsHdfsClient, deployGeWithDeps)

    println("SUCCESS! DeepSense.io deployment on HDFS succeeded!")
  }

  /**
   * Copies to HDFS all GE configuration files and uber-jar with GE code.
   * @param dsHdfsClient client for connection to HDFS
   */
  def deployOnHdfs(dsHdfsClient: DSHdfsClient, deployGeWithDeps: Boolean): Unit = {
    println("DeepSense.io deployment on HDFS: deploy also deps jar = " + deployGeWithDeps)
    // Delete previously deployed library and configuration file
    dsHdfsClient.hdfsClient.delete(geConfig.getString("deployment.lib.main.location"), true)
    if (deployGeWithDeps) {
      dsHdfsClient.hdfsClient.delete(geConfig.getString("deployment.lib.deps.location"), true)
    }
    dsHdfsClient.hdfsClient
      .delete(geConfig.getString("deployment.etc.applicationconf.location"), true)
    dsHdfsClient.hdfsClient.delete(geConfig.getString("deployment.etc.log4j.location"), true)

    // Create DeepSense.io directories on HDFS
    val deploymentRoot = geConfig.getString("deployment.root")
    dsHdfsClient.hdfsClient.mkdirs(
      deploymentRoot,
      new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.ALL),
      true)
    dsHdfsClient.hdfsClient.mkdirs(
      deploymentRoot + "/data",
      new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.ALL),
      true)
    dsHdfsClient.hdfsClient.mkdirs(
      deploymentRoot + "/tmp",
      new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.ALL),
      true)
    dsHdfsClient.hdfsClient.mkdirs(
      deploymentRoot + "/var/log",
      new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.ALL),
      true)
    dsHdfsClient.hdfsClient.mkdirs(
      deploymentRoot + "/lib",
      new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.ALL),
      true)
    dsHdfsClient.hdfsClient.mkdirs(
      deploymentRoot + "/etc",
      new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.ALL),
      true)

    // NOTE: We assume here that uber-jar has been assembled immediately before this task
    dsHdfsClient.copyLocalFile(log4jXmlPath, geConfig.getString("deployment.etc.log4j.location"))
    dsHdfsClient
      .copyLocalFile(geConfPath, geConfig.getString("deployment.etc.applicationconf.location"))
    dsHdfsClient.copyLocalFile(geJarPath, geConfig.getString("deployment.lib.main.location"))
    if (deployGeWithDeps) {
      dsHdfsClient.copyLocalFile(geDepsJarPath, geConfig.getString("deployment.lib.deps.location"))
    }
  }

  private def getHdfsAddressFromConfig(): String = {
    val geConfig = ConfigFactory.load
    val hdfsHostname = geConfig.getString("hdfs.hostname")
    val hdfsPort = geConfig.getString("hdfs.port")
    s"hdfs://$hdfsHostname:$hdfsPort"
  }
}

// scalastyle:on println
