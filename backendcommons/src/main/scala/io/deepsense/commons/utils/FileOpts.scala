/**
 * Copyright (c) 2016, CodiLime Inc.
 */
package io.deepsense.commons.utils

import java.io.File

object FileOpts {

  implicit class RichFile(file: File) {
    def createPathToFile(): Unit = {
      file.getParentFile.mkdirs()
    }
  }
}
