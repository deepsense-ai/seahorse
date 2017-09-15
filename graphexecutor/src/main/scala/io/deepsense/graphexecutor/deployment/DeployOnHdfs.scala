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
import io.deepsense.graphexecutor.Constants

// scalastyle:off println

/**
 * Performs DeepSense.io deployment on external HDFS cluster.
 */
object DeployOnHdfs {

  val geJarFilename = BuildInfo.name + "-assembly-" + BuildInfo.version + ".jar"
  val geDepsJarFilename = BuildInfo.name + "-assembly-" + BuildInfo.version + "-deps.jar"

  val geJarPath = s"../graphexecutor/target/scala-2.11/$geJarFilename"
  val geDepsJarPath = s"../graphexecutor/target/scala-2.11/$geDepsJarFilename"

  val geConfPath = getClass().getResource("/graphexecutor.conf").getPath
  val esConfPath = "../deeplang/target/scala-2.11/classes/entitystorage-communication.conf"

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
    dsHdfsClient.hdfsClient.delete(Constants.GraphExecutorJarLocation, true)
    if (deployGeWithDeps) {
      dsHdfsClient.hdfsClient.delete(Constants.GraphExecutorDepsJarLocation, true)
    }
    dsHdfsClient.hdfsClient.delete(Constants.GraphExecutorConfigLocation, true)
    dsHdfsClient.hdfsClient.delete(Constants.EntityStorageConfigLocation, true)

    // Create DeepSense.io directories on HDFS
    dsHdfsClient.hdfsClient.mkdirs(
      Constants.DeepSenseIoDeploymentDirectory,
      new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.ALL),
      true)
    dsHdfsClient.hdfsClient.mkdirs(
      Constants.DeepSenseIoDeploymentDirectory + "/data",
      new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.ALL),
      true)
    dsHdfsClient.hdfsClient.mkdirs(
      Constants.DeepSenseIoDeploymentDirectory + "/tmp",
      new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.ALL),
      true)
    dsHdfsClient.hdfsClient.mkdirs(
      Constants.DeepSenseIoDeploymentDirectory + "/var/log",
      new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.ALL),
      true)
    dsHdfsClient.hdfsClient.mkdirs(
      Constants.DeepSenseIoDeploymentDirectory + "/lib",
      new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.ALL),
      true)
    dsHdfsClient.hdfsClient.mkdirs(
      Constants.DeepSenseIoDeploymentDirectory + "/etc",
      new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.ALL),
      true)

    // NOTE: We assume here that uber-jar has been assembled immediately before this task
    dsHdfsClient.copyLocalFile(geJarPath, Constants.GraphExecutorJarLocation)
    if (deployGeWithDeps) {
      dsHdfsClient.copyLocalFile(geDepsJarPath, Constants.GraphExecutorDepsJarLocation)
    }
    dsHdfsClient.copyLocalFile(
      geConfPath,
      Constants.GraphExecutorConfigLocation)
    dsHdfsClient.copyLocalFile(
      esConfPath,
      Constants.EntityStorageConfigLocation)
  }

  private def getHdfsAddressFromConfig(): String = {
    val geConfig: Config = ConfigFactory.load(Constants.GraphExecutorConfName)
    val hdfsHostname = geConfig.getString("hdfs.hostname")
    val hdfsPort = geConfig.getString("hdfs.port")
    s"hdfs://$hdfsHostname:$hdfsPort"
  }
}

// scalastyle:on println
