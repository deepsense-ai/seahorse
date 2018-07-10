/**
 * Copyright 2016 deepsense.ai (CodiLime, Inc)
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

package ai.deepsense.deeplang.doperations.readwritedataframe.filestorage

import java.io._
import java.net.URL
import java.nio.file.{Files, Paths}
import java.util.UUID

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import ai.deepsense.deeplang.ExecutionContext
import ai.deepsense.deeplang.doperations.exceptions.DeepSenseIOException
import ai.deepsense.deeplang.doperations.readwritedataframe.FilePath

object FileDownloader {

  def downloadFile(url: String)(implicit context: ExecutionContext): FilePath = {
    if (context.tempPath.startsWith("hdfs://") && !context.sparkContext.isLocal) {
      downloadFileToHdfs(url)
    } else {
      downloadFileToDriver(url)
    }
  }

  private def downloadFileToHdfs(url: String)(implicit context: ExecutionContext) = {
    val hdfsPath = s"${context.tempPath}/${UUID.randomUUID()}${getSuffix(url)}"

    val configuration = new Configuration()
    val hdfs = FileSystem.get(configuration)
    val file = new Path(hdfsPath)
    val hdfsStream = hdfs.create(file)

    download(new URL(url), hdfsStream)
    FilePath(hdfsPath)
  }


  private def downloadFileToDriver(url: String)
                                  (implicit context: ExecutionContext) = {
    val outputDirPath = Paths.get(context.tempPath.stripPrefix("hdfs://"))
    // We're checking if the output is a directory following symlinks.
    // The default behaviour of createDirectories is NOT to follow symlinks
    if (!Files.isDirectory(outputDirPath)) {
      Files.createDirectories(outputDirPath)
    }
    val outFilePath = Files.createTempFile(outputDirPath, "download", getSuffix(url))
    download(new URL(url), outFilePath.toFile)
    FilePath(s"file:///$outFilePath")
  }

  // avro files need .avro suffix ot cryptic setting has to be set
  private def getSuffix(path: String, default: String = ".csv"): String = {
    val extensionRegex = """.*(\.\w+)""".r
    path match {
      case extensionRegex(matchedSuffix) => matchedSuffix
      case _ => ".csv"
    }
  }

  private def download(url: URL, file: OutputStream) = {
    import sys.process._
    import scala.language.postfixOps
    url #> file !!
  }


  private def download(url: URL, file: File) = {
    import sys.process._
    import scala.language.postfixOps
    url #> file !!
  }

  private def safeClose(bufferedWriter: BufferedWriter): Unit = {
    try {
      bufferedWriter.flush()
      bufferedWriter.close()
    } catch {
      case e: IOException => throw new DeepSenseIOException(e)
    }
  }

}
