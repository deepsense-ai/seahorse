/**
 * Copyright 2016, deepsense.io
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

package io.deepsense.deeplang.doperations.readwritedataframe

import io.deepsense.deeplang.exceptions.DeepLangException

sealed abstract class FileScheme(val schema: String) {
  def pathPrefix: String = schema + "://"
}

object FileScheme {

  case object HTTP extends FileScheme("http")
  case object HTTPS extends FileScheme("https")
  case object FTP extends FileScheme("ftp")
  case object HDFS extends FileScheme("hdfs")
  case object File extends FileScheme("file")
  case object Library extends FileScheme("library")

  // TODO Autoderive values. There is macro-library for extracting sealed case objects.
  val values = Seq(HTTP, HTTPS, FTP, HDFS, File, Library)

  def fromPath(path: String): FileScheme = {
    val matchingFileSchema = values.find(schema => path.startsWith(schema.pathPrefix))
    matchingFileSchema.getOrElse(throw UnknownFileSchemaForPath(path))
  }

}

case class FilePath(fileScheme: FileScheme, pathWithoutScheme: String) {
  def fullPath: String = fileScheme.pathPrefix + pathWithoutScheme
  def verifyScheme(assertedFileScheme: FileScheme): Unit = assert(fileScheme == assertedFileScheme)
}

object FilePath {
  def apply(fullPath: String): FilePath = {
    val schema = FileScheme.fromPath(fullPath)
    val pathWithoutSchema = fullPath.substring(schema.pathPrefix.length)
    FilePath(schema, pathWithoutSchema)
  }
}

case class UnknownFileSchemaForPath(path: String) extends DeepLangException({
  val allSchemas = FileScheme.values.map(_.schema).mkString("(", ", ", ")")
  s"Unknown file schema for path $path. Known file schemas: $allSchemas"
})
