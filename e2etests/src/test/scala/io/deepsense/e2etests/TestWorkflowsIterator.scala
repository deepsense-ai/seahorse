/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.e2etests

import java.io.File

object TestWorkflowsIterator {
  private val testsDirUri = getClass.getResource(s"/workflows").toURI
  private val testsDir = new File(testsDirUri.getPath)

  case class Input(path: String, fileContents: String)

  def foreach[T](f: Input => T): Unit = foreachInDirectory(f, testsDir)

  private def foreachInDirectory[T](f: Input => T, dir: File): Unit = {
    dir.listFiles.filter(!_.getName.startsWith("IGNORED")).foreach { file =>
      if (file.isFile) {
        val relativePath = testsDirUri.relativize(file.toURI)
        val source = scala.io.Source.fromFile(file)
        val contents = try source.getLines().mkString("\n") finally source.close()
        f(Input(relativePath.toString, contents))
      } else {
        foreachInDirectory(f, file)
      }
    }
  }
}
