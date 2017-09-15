/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.e2etests

import java.io.File
import java.net.URI

object TestWorkflowsIterator {
  private val testsDirUri = getClass.getResource(s"/workflows").toURI
  private val testsDir = new File(testsDirUri.getPath)

  case class Input(path: URI, file: File, fileContents: String)

  def foreach(f: Input => Unit): Unit = foreachInDirectory(f, testsDir)

  private def foreachInDirectory(f: Input => Unit, dir: File): Unit = {
    dir.listFiles.filter(!_.getName.startsWith("IGNORED")).foreach { file =>
      if (file.isFile) {
        val relativePath = testsDirUri.relativize(file.toURI)
        val source = scala.io.Source.fromFile(file)
        val contents = try source.getLines().mkString("\n") finally source.close()
        f(Input(relativePath, file, contents))
      } else {
        foreachInDirectory(f, file)
      }
    }
  }
}
