/**
 * Copyright 2015, CodiLime Inc.
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

package io.deepsense.deeplang.parameters

/**
 * Types of storage for IO DOperations.
 */
object StorageType extends Enumeration {

  val pathValidator = new AcceptAllRegexValidator

  def getPathWithProtocolPrefix(storageTypeStr: String, uri: String): String = {
    s"$storageTypeStr://${stripProtocol(storageTypeStr, uri)}"
  }

  private def stripProtocol(storageTypeStr: String, uri: String): String = {
    val protocolPrefix = storageTypeStr + "://"
    if (uri.toLowerCase.startsWith(protocolPrefix)) {
      uri.substring(protocolPrefix.length)
    } else {
      uri
    }
  }

  type StorageType = Value
  val HDFS = Value("hdfs")
  val S3N = Value("s3n")
  val FILE = Value("file")
  val LOCAL = Value("local")
}
