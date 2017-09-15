/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.libraryservice

import java.io.File

object FileOperations {
  def deleteRecursively(f: File): Boolean = {
    if (f.isFile) {
      f.delete()
    } else {
      f.listFiles.map(deleteRecursively)
      f.delete()
    }
  }
}
