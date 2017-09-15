/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.entitystorage.services

import java.io.InputStream
import java.net.URI

import scala.concurrent.Future

import com.google.inject.Inject
import com.google.inject.name.Named
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hdfs.DFSClient

import io.deepsense.deeplang.DSHdfsClient
import io.deepsense.entitystorage.UniqueFilenameUtil
import io.deepsense.models.entities._

class FileUploadService @Inject() (
    @Named("hdfs.hostname") hdfsHostname: String,
    @Named("hdfs.port") hdfsPort: String,
    entityService: EntityService) {

  val dsHdfsClient = new DSHdfsClient(
    new DFSClient(new URI(s"hdfs://$hdfsHostname:$hdfsPort"), new Configuration()))

  def uploadFile(tenantId: String, name: String, content: InputStream): Future[Entity.Id] = {

    val hdfsFile = UniqueFilenameUtil.getUniqueFsFilename(
      tenantId, UniqueFilenameUtil.FileEntityCategory)

    dsHdfsClient.saveInputStreamToFile(content, hdfsFile)

    val fileEntity = CreateEntityRequest(
      tenantId = tenantId,
      name = name,
      description = "Uploaded file",
      dClass = "File",
      dataReference = Some(DataObjectReference(hdfsFile, "{}")),
      report = DataObjectReport("{}"))
    entityService.createEntity(fileEntity)
  }
}
