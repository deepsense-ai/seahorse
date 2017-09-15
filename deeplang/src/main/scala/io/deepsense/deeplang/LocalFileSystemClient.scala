/**
 * Copyright 2015, deepsense.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.deepsense.deeplang

import java.io._
import java.nio.file.{Files, Paths}

import org.apache.hadoop.fs.FileUtil

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.commons.serialization.Serialization

case class LocalFileSystemClient() extends FileSystemClient with Serialization {

  override def fileExists(path: String): Boolean = Files.exists(Paths.get(path))

  override def copyLocalFile[T <: Serializable](
    localFilePath: String,
    remoteFilePath: String): Unit = {
    def copyFile(f: File, dest: String): Unit = {
      val fis: FileInputStream = new FileInputStream(f)
      try {
        saveInputStreamToFile(fis, dest)
      } finally {
        fis.close()
      }
    }
    val input = new File(localFilePath)
    if (input.isDirectory) {
      input.listFiles().foreach {f => copyFile(f, remoteFilePath + "/" + f.getName)}
    } else {
      copyFile(input, remoteFilePath)
    }

  }

  override def saveObjectToFile[T <: Serializable](path: String, instance: T): Unit = {
    val inputStream = new BufferedInputStream(new ByteArrayInputStream(serialize(instance)))
    try {
      saveInputStreamToFile(inputStream, path)
    } finally {
      inputStream.close()
    }
  }

  override def saveInputStreamToFile(inputStream: InputStream, destinationPath: String): Unit = {
    val fos = new BufferedOutputStream(new FileOutputStream(destinationPath))
    try {
      org.apache.commons.io.IOUtils.copy(inputStream, fos)
    } finally {
      fos.close()
    }
  }

  override def readFileAsObject[T <: Serializable](path: String): T = {
    val inputStream: FileInputStream = new FileInputStream(path)
    try {
      deserialize(org.apache.commons.io.IOUtils.toByteArray(inputStream))
    } finally {
      inputStream.close()
    }
  }

  override def getFileInfo(path: String): Option[FileInfo] = {
    val file = new File(path)
    if (file.exists()) {
      Some(FileInfo(file.length(), DateTimeConverter.fromMillis(file.lastModified())))
    } else {
      None
    }
  }

  override def delete(path: String): Unit = FileUtil.fullyDelete(new File(path))
}
