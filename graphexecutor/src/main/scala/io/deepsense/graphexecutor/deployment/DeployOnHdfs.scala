/**
 * Copyright (c) 2015, CodiLime, Inc.
 */
package io.deepsense.graphexecutor.deployment

import java.net.URI

import buildinfo.BuildInfo
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.permission.{FsAction, FsPermission}
import org.apache.hadoop.hdfs.DFSClient

import io.deepsense.deeplang.DSHdfsClient
import io.deepsense.graphexecutor.Constants

/**
 * Performs DeepSense.io deployment on external HDFS cluster.
 */
object DeployOnHdfs {

  val uberJarFilename = BuildInfo.name + "-assembly-" + BuildInfo.version + ".jar"

  val geUberJarPath = s"../graphexecutor/target/scala-2.11/$uberJarFilename"

  def main(args: Array[String]): Unit = {
    println("DeepSense.io deployment on HDFS starts")

    val config = new Configuration()
    config.addResource("conf/hadoop/core-site.xml")
    config.addResource("conf/hadoop/yarn-site.xml")
    config.addResource("conf/hadoop/hdfs-site.xml")
    val dfsClient = new DFSClient(
      new URI("hdfs://" + Constants.MasterHostname + ":" + Constants.HdfsNameNodePort),
      config)
    val dsHdfsClient = new DSHdfsClient(dfsClient)
    deployOnHdfs(dsHdfsClient)

    println("SUCCESS! DeepSense.io deployment on HDFS succeeded!")
  }

  /**
   * Copies to HDFS all GE configuration files and uber-jar with GE code.
   * @param dsHdfsClient client for connection to HDFS
   */
  def deployOnHdfs(dsHdfsClient: DSHdfsClient): Unit = {
    // Delete previously deployed library and configuration file
    dsHdfsClient.hdfsClient.delete(Constants.GraphExecutorLibraryLocation, true)
    dsHdfsClient.hdfsClient.delete(Constants.GraphExecutorConfigLocation, true)

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
    dsHdfsClient.copyLocalFile(geUberJarPath, Constants.GraphExecutorLibraryLocation)
    dsHdfsClient.copyLocalFile(
      "../graphexecutor/src/main/resources/graphexecutor.conf",
      Constants.GraphExecutorConfigLocation)
  }
}
