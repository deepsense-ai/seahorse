/**
 * Copyright 2015 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang

import java.io._
import java.nio.file.{Files, Paths}

import org.apache.hadoop.fs.FileUtil

import ai.deepsense.commons.datetime.DateTimeConverter
import ai.deepsense.commons.resources.ManagedResource
import ai.deepsense.commons.serialization.Serialization

case class LocalFileSystemClient() extends FileSystemClient with Serialization {

  override def fileExists(path: String): Boolean = Files.exists(Paths.get(path))

  override def copyLocalFile[T <: Serializable](
    localFilePath: String,
    remoteFilePath: String): Unit = {
    def copyFile(f: File, dest: String): Unit = {
      ManagedResource(new FileInputStream(f)) { fis =>
        saveInputStreamToFile(fis, dest)
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
    ManagedResource(inputStream) { inputStream =>
      saveInputStreamToFile(inputStream, path)
    }
  }

  override def saveInputStreamToFile(inputStream: InputStream, destinationPath: String): Unit =
    ManagedResource(new BufferedOutputStream(new FileOutputStream(destinationPath))) { fos =>
      org.apache.commons.io.IOUtils.copy(inputStream, fos)
    }

  override def readFileAsObject[T <: Serializable](path: String): T =
    ManagedResource(new FileInputStream(path)) { inputStream =>
      deserialize(org.apache.commons.io.IOUtils.toByteArray(inputStream))
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
