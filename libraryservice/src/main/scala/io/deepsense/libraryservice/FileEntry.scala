/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.libraryservice

import spray.json.DefaultJsonProtocol

case class FileEntry(name: String)

object FileEntryJsonProtocol extends DefaultJsonProtocol {
  implicit val fileEntryJsonFormat = jsonFormat1(FileEntry.apply)
}
